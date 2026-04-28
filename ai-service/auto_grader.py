import re
from typing import Dict, List, Any, Optional


class AutoGrader:
    def __init__(self, config):
        self.config = config
        self.grading_config = config.GRADING_CONFIG
    
    def grade_submission(self, answers: List[Dict], questions: List[Dict]) -> Dict[str, Any]:
        graded_answers = []
        total_score = 0
        max_score = 0
        correct_count = 0
        total_count = 0
        
        question_map = {q.get('id') or q.get('question_id'): q for q in questions}
        
        for answer in answers:
            question_id = answer.get('question_id') or answer.get('id')
            student_answer = answer.get('student_answer', '')
            
            if question_id in question_map:
                question = question_map[question_id]
                question_type = question.get('type', '').upper()
                correct_answer = question.get('correct_answer', '')
                score = question.get('score', 0)
                
                max_score += score
                total_count += 1
                
                grade_result = self._grade_question(
                    question_type, student_answer, correct_answer, score
                )
                
                graded_answer = {
                    'question_id': question_id,
                    'question_type': question_type,
                    'student_answer': student_answer,
                    'correct_answer': correct_answer,
                    'score': grade_result.get('score', 0),
                    'max_score': score,
                    'is_correct': grade_result.get('is_correct', False),
                    'similarity': grade_result.get('similarity', 0),
                    'partial_correct': grade_result.get('partial_correct', False)
                }
                
                graded_answers.append(graded_answer)
                
                if grade_result.get('is_correct', False):
                    correct_count += 1
                total_score += grade_result.get('score', 0)
        
        accuracy = correct_count / total_count if total_count > 0 else 0
        
        return {
            'total_score': total_score,
            'max_score': max_score,
            'accuracy': accuracy,
            'correct_count': correct_count,
            'total_count': total_count,
            'graded_answers': graded_answers
        }
    
    def _grade_question(self, question_type: str, student_answer: str, 
                        correct_answer: str, max_score: int) -> Dict[str, Any]:
        question_type = question_type.upper()
        
        if question_type == 'SINGLE_CHOICE':
            return self.grade_single_choice(student_answer, correct_answer, max_score)
        elif question_type == 'MULTIPLE_CHOICE':
            return self.grade_multiple_choice(student_answer, correct_answer, max_score)
        elif question_type == 'TRUE_FALSE':
            return self.grade_true_false(student_answer, correct_answer, max_score)
        elif question_type == 'FILL_BLANK':
            return self.grade_fill_blank(student_answer, correct_answer, max_score)
        elif question_type == 'SHORT_ANSWER' or question_type == 'ESSAY':
            return self._grade_short_answer(student_answer, correct_answer, max_score)
        else:
            return {
                'is_correct': None,
                'score': None,
                'message': f'不支持自动批改的题型: {question_type}'
            }
    
    def grade_single_choice(self, student_answer: str, correct_answer: str, 
                            max_score: int) -> Dict[str, Any]:
        config = self.grading_config.get('single_choice', {})
        
        sa = self._normalize_answer(student_answer, config)
        ca = self._normalize_answer(correct_answer, config)
        
        is_correct = sa == ca
        score = max_score if is_correct else 0
        
        return {
            'is_correct': is_correct,
            'score': score,
            'similarity': 1.0 if is_correct else 0.0
        }
    
    def grade_multiple_choice(self, student_answer: str, correct_answer: str,
                               max_score: int) -> Dict[str, Any]:
        config = self.grading_config.get('multiple_choice', {})
        
        student_options = self._parse_multiple_choice(student_answer, config)
        correct_options = self._parse_multiple_choice(correct_answer, config)
        
        student_set = set(student_options)
        correct_set = set(correct_options)
        
        correct_selected = student_set & correct_set
        wrong_selected = student_set - correct_set
        missed = correct_set - student_set
        
        is_correct = (len(correct_selected) == len(correct_set) and len(wrong_selected) == 0)
        partial_correct = len(correct_selected) > 0 and not is_correct
        
        if config.get('partial_credit', False):
            correct_ratio = len(correct_selected) / len(correct_set) if len(correct_set) > 0 else 0
            wrong_penalty = len(wrong_selected) / max(len(student_set), 1)
            score = int(max_score * correct_ratio * (1 - wrong_penalty * 0.5))
            score = max(0, score)
        else:
            score = max_score if is_correct else 0
        
        similarity = len(correct_selected) / len(correct_set | student_set) if len(correct_set | student_set) > 0 else 0.0
        
        return {
            'is_correct': is_correct,
            'partial_correct': partial_correct,
            'score': score,
            'similarity': similarity,
            'correct_selected': list(correct_selected),
            'wrong_selected': list(wrong_selected),
            'missed': list(missed)
        }
    
    def grade_true_false(self, student_answer: str, correct_answer: str,
                          max_score: int) -> Dict[str, Any]:
        config = self.grading_config.get('true_false', {})
        accepted_values = config.get('accepted_values', {})
        
        sa_normalized = self._normalize_true_false(student_answer, accepted_values)
        ca_normalized = self._normalize_true_false(correct_answer, accepted_values)
        
        is_correct = sa_normalized == ca_normalized
        score = max_score if is_correct else 0
        
        return {
            'is_correct': is_correct,
            'score': score,
            'similarity': 1.0 if is_correct else 0.0,
            'normalized_student_answer': sa_normalized,
            'normalized_correct_answer': ca_normalized
        }
    
    def grade_fill_blank(self, student_answer: str, correct_answer: str,
                          max_score: int) -> Dict[str, Any]:
        config = self.grading_config.get('fill_blank', {})
        threshold = config.get('similarity_threshold', 0.8)
        use_fuzzy = config.get('use_fuzzy_matching', True)
        
        sa = self._normalize_answer(student_answer, config)
        ca = self._normalize_answer(correct_answer, config)
        
        if ca == '':
            return {
                'is_correct': None,
                'score': None,
                'similarity': 0.0,
                'message': '正确答案为空'
            }
        
        if use_fuzzy:
            similarity = self._calculate_similarity(sa, ca)
        else:
            similarity = 1.0 if sa == ca else 0.0
        
        is_correct = similarity >= threshold
        score = max_score if is_correct else 0
        
        return {
            'is_correct': is_correct,
            'score': score,
            'similarity': similarity,
            'threshold': threshold
        }
    
    def _grade_short_answer(self, student_answer: str, correct_answer: str,
                             max_score: int) -> Dict[str, Any]:
        if correct_answer:
            similarity = self._calculate_similarity(student_answer, correct_answer)
            threshold = 0.7
            
            if similarity >= threshold:
                score = int(max_score * similarity)
                is_correct = similarity >= 0.9
            else:
                score = 0
                is_correct = False
            
            return {
                'is_correct': is_correct,
                'score': score,
                'similarity': similarity,
                'message': '简答题需要教师最终确认'
            }
        else:
            return {
                'is_correct': None,
                'score': None,
                'message': '简答题需要人工批改'
            }
    
    def _normalize_answer(self, answer: str, config: Dict) -> str:
        if not answer:
            return ''
        
        result = str(answer).strip()
        
        if config.get('trim_whitespace', True):
            result = result.strip()
            result = re.sub(r'\s+', ' ', result)
        
        if config.get('case_insensitive', True):
            result = result.upper()
        
        return result
    
    def _parse_multiple_choice(self, answer: str, config: Dict) -> List[str]:
        if not answer:
            return []
        
        separators = config.get('separator', [',', ';', ' '])
        pattern = '[' + re.escape(''.join(separators)) + ']+'
        
        parts = re.split(pattern, str(answer).strip())
        parts = [p.strip() for p in parts if p.strip()]
        
        if config.get('case_insensitive', True):
            parts = [p.upper() for p in parts]
        
        return parts
    
    def _normalize_true_false(self, answer: str, accepted_values: Dict) -> Optional[str]:
        if not answer:
            return None
        
        answer_lower = str(answer).strip().lower()
        
        for value, alternatives in accepted_values.items():
            if answer_lower in [alt.lower() for alt in alternatives]:
                return value.upper()
        
        return answer_lower.upper()
    
    def _calculate_similarity(self, str1: str, str2: str) -> float:
        if not str1 or not str2:
            return 0.0
        
        s1 = str1.lower()
        s2 = str2.lower()
        
        if s1 == s2:
            return 1.0
        
        len1, len2 = len(s1), len(s2)
        if len1 == 0 or len2 == 0:
            return 0.0
        
        set1 = set(s1)
        set2 = set(s2)
        intersection = set1 & set2
        union = set1 | set2
        
        if len(union) == 0:
            return 0.0
        
        char_similarity = len(intersection) / len(union)
        
        distance = self._levenshtein_distance(s1, s2)
        max_len = max(len1, len2)
        edit_similarity = 1 - (distance / max_len) if max_len > 0 else 0
        
        return (char_similarity + edit_similarity) / 2
    
    def _levenshtein_distance(self, s1: str, s2: str) -> int:
        if len(s1) < len(s2):
            return self._levenshtein_distance(s2, s1)
        
        if len(s2) == 0:
            return len(s1)
        
        previous_row = list(range(len(s2) + 1))
        for i, c1 in enumerate(s1):
            current_row = [i + 1]
            for j, c2 in enumerate(s2):
                insertions = previous_row[j + 1] + 1
                deletions = current_row[j] + 1
                substitutions = previous_row[j] + (c1 != c2)
                current_row.append(min(insertions, deletions, substitutions))
            previous_row = current_row
        
        return previous_row[-1]
