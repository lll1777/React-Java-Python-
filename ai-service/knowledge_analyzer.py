from typing import Dict, List, Any, Optional
from collections import defaultdict, Counter
import json


class KnowledgeAnalyzer:
    def __init__(self, config):
        self.config = config
        self.analysis_config = config.KNOWLEDGE_ANALYSIS_CONFIG
    
    def analyze_student_knowledge(self, student_id: int, 
                                    wrong_questions: List[Dict],
                                    submissions: List[Dict]) -> Dict[str, Any]:
        knowledge_points = self._extract_knowledge_points(wrong_questions, submissions)
        
        weak_points = []
        strong_points = []
        all_points = []
        
        for kp, data in knowledge_points.items():
            total = data.get('total_attempts', 0)
            wrong = data.get('wrong_count', 0)
            correct = total - wrong
            
            accuracy = correct / total if total > 0 else 0
            
            point_info = {
                'knowledge_point': kp,
                'total_attempts': total,
                'correct_count': correct,
                'wrong_count': wrong,
                'accuracy': accuracy,
                'questions': data.get('questions', [])
            }
            
            all_points.append(point_info)
            
            if accuracy < self.analysis_config.get('weak_point_threshold', 0.6):
                weak_points.append(point_info)
            elif accuracy > self.analysis_config.get('strong_point_threshold', 0.8):
                strong_points.append(point_info)
        
        weak_points.sort(key=lambda x: x['wrong_count'], reverse=True)
        strong_points.sort(key=lambda x: x['accuracy'], reverse=True)
        
        analysis = self._generate_analysis_text(
            student_id, all_points, weak_points, strong_points,
            len(wrong_questions), len(submissions)
        )
        
        recommendations = self._generate_recommendations(weak_points, strong_points)
        improvement_suggestions = self._generate_improvement_suggestions(weak_points)
        
        return {
            'student_id': student_id,
            'analysis': analysis,
            'knowledge_points': all_points,
            'weak_points': weak_points,
            'strong_points': strong_points,
            'recommendations': recommendations,
            'improvement_suggestions': improvement_suggestions,
            'total_wrong_questions': len(wrong_questions),
            'total_submissions': len(submissions)
        }
    
    def _extract_knowledge_points(self, wrong_questions: List[Dict],
                                   submissions: List[Dict]) -> Dict[str, Dict]:
        knowledge_points = defaultdict(lambda: {
            'total_attempts': 0,
            'wrong_count': 0,
            'correct_count': 0,
            'questions': []
        })
        
        for wq in wrong_questions:
            question = wq.get('question', {}) or {}
            kp_list = self._parse_knowledge_points(question.get('knowledgePoints', ''))
            
            for kp in kp_list:
                knowledge_points[kp]['total_attempts'] += wq.get('wrongCount', 1)
                knowledge_points[kp]['wrong_count'] += wq.get('wrongCount', 1)
                knowledge_points[kp]['questions'].append({
                    'question_id': question.get('id'),
                    'content': question.get('content', '')[:100] if question.get('content') else '',
                    'wrong_count': wq.get('wrongCount', 1),
                    'is_resolved': wq.get('isResolved', False)
                })
        
        for submission in submissions:
            if submission.get('status') in ['GRADED', 'RETURNED', 'ARCHIVED']:
                answers = submission.get('answers', [])
                for answer in answers:
                    question = answer.get('question', {}) or {}
                    kp_list = self._parse_knowledge_points(question.get('knowledgePoints', ''))
                    
                    for kp in kp_list:
                        knowledge_points[kp]['total_attempts'] += 1
                        
                        if answer.get('isCorrect'):
                            knowledge_points[kp]['correct_count'] += 1
        
        return dict(knowledge_points)
    
    def _parse_knowledge_points(self, kp_str: str) -> List[str]:
        if not kp_str:
            return ['未分类']
        
        separators = [',', ';', '，', '；']
        for sep in separators:
            if sep in kp_str:
                kp_list = [kp.strip() for kp in kp_str.split(sep) if kp.strip()]
                return kp_list if kp_list else ['未分类']
        
        return [kp_str.strip()] if kp_str.strip() else ['未分类']
    
    def _generate_analysis_text(self, student_id: int, all_points: List[Dict],
                                 weak_points: List[Dict], strong_points: List[Dict],
                                 total_wrong: int, total_submissions: int) -> str:
        lines = []
        
        lines.append(f"学生学习情况分析报告（学生ID: {student_id}）")
        lines.append("=" * 50)
        lines.append("")
        
        lines.append(f"【整体概况】")
        lines.append(f"- 总提交作业数: {total_submissions} 份")
        lines.append(f"- 累计错题数: {total_wrong} 道")
        lines.append(f"- 知识点数量: {len(all_points)} 个")
        lines.append(f"- 薄弱知识点: {len(weak_points)} 个")
        lines.append(f"- 掌握较好知识点: {len(strong_points)} 个")
        lines.append("")
        
        if weak_points:
            lines.append("【薄弱知识点分析】")
            for i, wp in enumerate(weak_points[:5], 1):
                accuracy = wp.get('accuracy', 0) * 100
                lines.append(f"{i}. {wp['knowledge_point']}")
                lines.append(f"   - 正确率: {accuracy:.1f}%")
                lines.append(f"   - 错误次数: {wp['wrong_count']} 次")
                lines.append(f"   - 总练习次数: {wp['total_attempts']} 次")
            lines.append("")
        
        if strong_points:
            lines.append("【掌握较好知识点】")
            for i, sp in enumerate(strong_points[:5], 1):
                accuracy = sp.get('accuracy', 0) * 100
                lines.append(f"{i}. {sp['knowledge_point']}")
                lines.append(f"   - 正确率: {accuracy:.1f}%")
                lines.append(f"   - 总练习次数: {sp['total_attempts']} 次")
            lines.append("")
        
        if all_points:
            all_points_sorted = sorted(all_points, key=lambda x: x['accuracy'])
            avg_accuracy = sum(p['accuracy'] for p in all_points) / len(all_points) * 100
            lines.append(f"【整体准确率】{avg_accuracy:.1f}%")
            lines.append("")
        
        return "\n".join(lines)
    
    def _generate_recommendations(self, weak_points: List[Dict],
                                   strong_points: List[Dict]) -> str:
        recommendations = []
        
        if not weak_points:
            recommendations.append("您的知识点掌握情况良好，继续保持！")
        else:
            recommendations.append("【学习建议】")
            recommendations.append("")
            
            recommendations.append("1. 优先攻克薄弱知识点：")
            for wp in weak_points[:3]:
                recommendations.append(f"   - {wp['knowledge_point']}（错误{wp['wrong_count']}次）")
            
            recommendations.append("")
            recommendations.append("2. 建议的学习路径：")
            recommendations.append("   a. 复习相关概念和公式")
            recommendations.append("   b. 重做错题，理解错误原因")
            recommendations.append("   c. 找类似题目进行练习巩固")
            recommendations.append("   d. 定期回顾，避免遗忘")
            
            recommendations.append("")
            if strong_points:
                recommendations.append("3. 发挥优势：")
                for sp in strong_points[:2]:
                    recommendations.append(f"   - {sp['knowledge_point']} 掌握较好，可挑战更难的题目")
        
        return "\n".join(recommendations)
    
    def _generate_improvement_suggestions(self, weak_points: List[Dict]) -> List[Dict]:
        suggestions = []
        
        for wp in weak_points:
            suggestion = {
                'knowledge_point': wp['knowledge_point'],
                'priority': '高' if wp['wrong_count'] > 3 else '中',
                'suggestions': [
                    f"复习「{wp['knowledge_point']}」相关的基础概念",
                    f"重做该知识点的错题，共{wp['wrong_count']}道",
                    f"找5-10道类似题目进行巩固练习",
                    f"建议在3天内完成复习并记录学习笔记"
                ],
                'estimated_time': f"{wp['wrong_count'] * 15} 分钟",
                'expected_outcome': f"目标正确率提升至80%以上"
            }
            suggestions.append(suggestion)
        
        return suggestions
    
    def compare_students_knowledge(self, students_data: List[Dict]) -> Dict[str, Any]:
        all_knowledge_points = set()
        student_comparison = []
        
        for student_data in students_data:
            student_id = student_data.get('student_id')
            wrong_questions = student_data.get('wrong_questions', [])
            submissions = student_data.get('submissions', [])
            
            analysis = self.analyze_student_knowledge(student_id, wrong_questions, submissions)
            
            for kp in analysis.get('knowledge_points', []):
                all_knowledge_points.add(kp['knowledge_point'])
            
            student_comparison.append({
                'student_id': student_id,
                'analysis': analysis,
                'weak_points_count': len(analysis.get('weak_points', [])),
                'strong_points_count': len(analysis.get('strong_points', [])),
                'total_wrong': analysis.get('total_wrong_questions', 0)
            })
        
        kp_comparison = []
        for kp in sorted(all_knowledge_points):
            kp_data = {
                'knowledge_point': kp,
                'students': []
            }
            
            for sc in student_comparison:
                for sp in sc['analysis'].get('knowledge_points', []):
                    if sp['knowledge_point'] == kp:
                        kp_data['students'].append({
                            'student_id': sc['student_id'],
                            'accuracy': sp['accuracy'],
                            'wrong_count': sp['wrong_count']
                        })
            
            if kp_data['students']:
                avg_accuracy = sum(s['accuracy'] for s in kp_data['students']) / len(kp_data['students'])
                kp_data['average_accuracy'] = avg_accuracy
                kp_comparison.append(kp_data)
        
        return {
            'total_students': len(student_comparison),
            'total_knowledge_points': len(all_knowledge_points),
            'student_comparison': student_comparison,
            'knowledge_point_comparison': kp_comparison
        }
