class Config:
    DEBUG = True
    SECRET_KEY = 'ai-grading-service-secret-key-2024'
    
    JAVA_BACKEND_URL = 'http://localhost:8080'
    
    GRADING_CONFIG = {
        'single_choice': {
            'case_insensitive': True,
            'trim_whitespace': True
        },
        'multiple_choice': {
            'case_insensitive': True,
            'trim_whitespace': True,
            'separator': [',', ';', ' '],
            'partial_credit': False,
            'partial_credit_ratio': 0.5
        },
        'true_false': {
            'case_insensitive': True,
            'accepted_values': {
                'true': ['true', 't', '1', 'yes', '对', '正确'],
                'false': ['false', 'f', '0', 'no', '错', '错误']
            }
        },
        'fill_blank': {
            'case_insensitive': True,
            'trim_whitespace': True,
            'similarity_threshold': 0.8,
            'use_fuzzy_matching': True
        }
    }
    
    KNOWLEDGE_ANALYSIS_CONFIG = {
        'weak_point_threshold': 0.6,
        'strong_point_threshold': 0.8,
        'max_recommendations': 5,
        'min_questions_for_analysis': 3
    }
