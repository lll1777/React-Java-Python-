from flask import Flask, request, jsonify
from flask_cors import CORS
import json

from auto_grader import AutoGrader
from knowledge_analyzer import KnowledgeAnalyzer
from config import Config

app = Flask(__name__)
CORS(app)

config = Config()
auto_grader = AutoGrader(config)
knowledge_analyzer = KnowledgeAnalyzer(config)


@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'healthy',
        'service': 'ai-grading-service',
        'version': '1.0.0'
    })


@app.route('/api/auto-grade/<int:submission_id>', methods=['POST'])
def auto_grade_submission(submission_id):
    try:
        request_data = request.get_json() if request.is_json else None
        
        if request_data and 'answers' in request_data:
            answers = request_data.get('answers', [])
            questions = request_data.get('questions', [])
            
            result = auto_grader.grade_submission(answers, questions)
            
            return jsonify({
                'success': True,
                'submission_id': submission_id,
                'total_score': result.get('total_score', 0),
                'max_score': result.get('max_score', 0),
                'accuracy': result.get('accuracy', 0),
                'graded_answers': result.get('graded_answers', []),
                'message': '自动批改完成'
            })
        else:
            return jsonify({
                'success': True,
                'submission_id': submission_id,
                'total_score': 0,
                'message': '等待从Java后端获取提交数据'
            })
            
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'自动批改失败: {str(e)}'
        }), 500


@app.route('/api/auto-grade/batch', methods=['POST'])
def auto_grade_batch():
    try:
        request_data = request.get_json()
        submissions = request_data.get('submissions', [])
        
        results = []
        for submission in submissions:
            submission_id = submission.get('submission_id')
            answers = submission.get('answers', [])
            questions = submission.get('questions', [])
            
            result = auto_grader.grade_submission(answers, questions)
            results.append({
                'submission_id': submission_id,
                'result': result
            })
        
        return jsonify({
            'success': True,
            'total_submissions': len(results),
            'results': results
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'批量批改失败: {str(e)}'
        }), 500


@app.route('/api/knowledge-analysis/<int:student_id>', methods=['POST'])
def analyze_knowledge(student_id):
    try:
        request_data = request.get_json() if request.is_json else None
        
        if request_data:
            wrong_questions = request_data.get('wrong_questions', [])
            submissions = request_data.get('submissions', [])
            
            analysis = knowledge_analyzer.analyze_student_knowledge(
                student_id, wrong_questions, submissions
            )
            
            return jsonify({
                'success': True,
                'student_id': student_id,
                'analysis': analysis.get('analysis', ''),
                'knowledge_points': analysis.get('knowledge_points', []),
                'weak_points': analysis.get('weak_points', []),
                'strong_points': analysis.get('strong_points', []),
                'recommendations': analysis.get('recommendations', ''),
                'improvement_suggestions': analysis.get('improvement_suggestions', [])
            })
        else:
            return jsonify({
                'success': True,
                'student_id': student_id,
                'message': '等待从Java后端获取学生数据'
            })
            
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'知识点分析失败: {str(e)}'
        }), 500


@app.route('/api/knowledge-analysis/compare', methods=['POST'])
def compare_knowledge():
    try:
        request_data = request.get_json()
        students_data = request_data.get('students_data', [])
        
        comparison = knowledge_analyzer.compare_students_knowledge(students_data)
        
        return jsonify({
            'success': True,
            'comparison': comparison
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'message': f'对比分析失败: {str(e)}'
        }), 500


@app.route('/api/grade/single-choice', methods=['POST'])
def grade_single_choice():
    try:
        request_data = request.get_json()
        student_answer = request_data.get('student_answer', '')
        correct_answer = request_data.get('correct_answer', '')
        score = request_data.get('score', 0)
        
        result = auto_grader.grade_single_choice(student_answer, correct_answer, score)
        
        return jsonify({
            'success': True,
            'is_correct': result.get('is_correct', False),
            'score': result.get('score', 0),
            'max_score': score
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500


@app.route('/api/grade/multiple-choice', methods=['POST'])
def grade_multiple_choice():
    try:
        request_data = request.get_json()
        student_answer = request_data.get('student_answer', '')
        correct_answer = request_data.get('correct_answer', '')
        score = request_data.get('score', 0)
        
        result = auto_grader.grade_multiple_choice(student_answer, correct_answer, score)
        
        return jsonify({
            'success': True,
            'is_correct': result.get('is_correct', False),
            'score': result.get('score', 0),
            'max_score': score,
            'partial_correct': result.get('partial_correct', False)
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500


@app.route('/api/grade/true-false', methods=['POST'])
def grade_true_false():
    try:
        request_data = request.get_json()
        student_answer = request_data.get('student_answer', '')
        correct_answer = request_data.get('correct_answer', '')
        score = request_data.get('score', 0)
        
        result = auto_grader.grade_true_false(student_answer, correct_answer, score)
        
        return jsonify({
            'success': True,
            'is_correct': result.get('is_correct', False),
            'score': result.get('score', 0),
            'max_score': score
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500


@app.route('/api/grade/fill-blank', methods=['POST'])
def grade_fill_blank():
    try:
        request_data = request.get_json()
        student_answer = request_data.get('student_answer', '')
        correct_answer = request_data.get('correct_answer', '')
        score = request_data.get('score', 0)
        
        result = auto_grader.grade_fill_blank(student_answer, correct_answer, score)
        
        return jsonify({
            'success': True,
            'is_correct': result.get('is_correct', False),
            'score': result.get('score', 0),
            'max_score': score,
            'similarity': result.get('similarity', 0)
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500


if __name__ == '__main__':
    print('=' * 50)
    print('  AI 自动批改与知识点分析服务')
    print('  Python Flask Service - Port: 5000')
    print('=' * 50)
    print('API Endpoints:')
    print('  - GET  /api/health')
    print('  - POST /api/auto-grade/<submission_id>')
    print('  - POST /api/auto-grade/batch')
    print('  - POST /api/knowledge-analysis/<student_id>')
    print('  - POST /api/knowledge-analysis/compare')
    print('  - POST /api/grade/single-choice')
    print('  - POST /api/grade/multiple-choice')
    print('  - POST /api/grade/true-false')
    print('  - POST /api/grade/fill-blank')
    print('=' * 50)
    
    app.run(host='0.0.0.0', port=5000, debug=True)
