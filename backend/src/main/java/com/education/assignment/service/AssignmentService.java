package com.education.assignment.service;

import com.education.assignment.dto.AssignmentCreateDTO;
import com.education.assignment.entity.Assignment;
import com.education.assignment.entity.Clazz;
import com.education.assignment.entity.Question;
import com.education.assignment.entity.User;
import com.education.assignment.enums.AssignmentEvent;
import com.education.assignment.enums.AssignmentStatus;
import com.education.assignment.enums.QuestionType;
import com.education.assignment.repository.AssignmentRepository;
import com.education.assignment.repository.ClazzRepository;
import com.education.assignment.repository.QuestionRepository;
import com.education.assignment.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final ClazzRepository clazzRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final StateMachineFactory<AssignmentStatus, AssignmentEvent> stateMachineFactory;

    @Transactional
    public Assignment createAssignment(AssignmentCreateDTO dto, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Clazz clazz = clazzRepository.findById(dto.getClassId())
                .orElseThrow(() -> new RuntimeException("班级不存在"));
        
        Assignment assignment = new Assignment();
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setClazz(clazz);
        assignment.setCreator(creator);
        assignment.setStatus(AssignmentStatus.DRAFT);
        assignment.setDeadline(dto.getDeadline());
        assignment.setTotalScore(dto.getTotalScore());
        
        Assignment savedAssignment = assignmentRepository.save(assignment);
        
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            List<Question> questions = new ArrayList<>();
            for (AssignmentCreateDTO.QuestionCreateDTO qDto : dto.getQuestions()) {
                Question question = new Question();
                question.setAssignment(savedAssignment);
                question.setOrderIndex(qDto.getOrderIndex() != null ? qDto.getOrderIndex() : questions.size() + 1);
                question.setType(QuestionType.valueOf(qDto.getType()));
                question.setContent(qDto.getContent());
                question.setOptions(qDto.getOptions());
                question.setCorrectAnswer(qDto.getCorrectAnswer());
                question.setScore(qDto.getScore());
                question.setKnowledgePoints(qDto.getKnowledgePoints());
                question.setDifficulty(qDto.getDifficulty());
                question.setExplanation(qDto.getExplanation());
                question.setAutoGradable(qDto.getAutoGradable() != null ? qDto.getAutoGradable() : isAutoGradable(QuestionType.valueOf(qDto.getType())));
                questions.add(question);
            }
            questionRepository.saveAll(questions);
            savedAssignment.setQuestions(questions);
        }
        
        return savedAssignment;
    }

    private boolean isAutoGradable(QuestionType type) {
        return type == QuestionType.SINGLE_CHOICE ||
               type == QuestionType.MULTIPLE_CHOICE ||
               type == QuestionType.TRUE_FALSE ||
               type == QuestionType.FILL_BLANK;
    }

    @Transactional
    public Assignment publishAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.DRAFT) {
            throw new RuntimeException("只有草稿状态的作业可以发布");
        }
        
        StateMachine<AssignmentStatus, AssignmentEvent> sm = stateMachineFactory.getStateMachine();
        sm.start();
        
        assignment.setStatus(AssignmentStatus.PUBLISHED);
        assignment.setPublishTime(LocalDateTime.now());
        
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment openAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.PUBLISHED) {
            throw new RuntimeException("只有已发布状态的作业可以开启");
        }
        
        assignment.setStatus(AssignmentStatus.OPEN);
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment archiveAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.RETURNED) {
            throw new RuntimeException("只有已返回状态的作业可以归档");
        }
        
        assignment.setStatus(AssignmentStatus.ARCHIVED);
        return assignmentRepository.save(assignment);
    }

    public Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
    }

    public List<Assignment> getAssignmentsByClassId(Long classId) {
        return assignmentRepository.findByClazzId(classId);
    }

    public List<Assignment> getAssignmentsByStudentId(Long studentId) {
        return assignmentRepository.findByStudentId(studentId);
    }

    public List<Assignment> getAssignmentsByCreatorId(Long creatorId) {
        return assignmentRepository.findByCreatorId(creatorId);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.DRAFT) {
            throw new RuntimeException("只能删除草稿状态的作业");
        }
        
        assignmentRepository.delete(assignment);
    }

    @Transactional
    public Assignment updateAssignment(Long id, AssignmentCreateDTO dto) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.DRAFT) {
            throw new RuntimeException("只能修改草稿状态的作业");
        }
        
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDeadline(dto.getDeadline());
        assignment.setTotalScore(dto.getTotalScore());
        
        if (dto.getQuestions() != null && !dto.getQuestions().isEmpty()) {
            questionRepository.deleteAll(assignment.getQuestions());
            assignment.getQuestions().clear();
            
            for (AssignmentCreateDTO.QuestionCreateDTO qDto : dto.getQuestions()) {
                Question question = new Question();
                question.setAssignment(assignment);
                question.setOrderIndex(qDto.getOrderIndex() != null ? qDto.getOrderIndex() : assignment.getQuestions().size() + 1);
                question.setType(QuestionType.valueOf(qDto.getType()));
                question.setContent(qDto.getContent());
                question.setOptions(qDto.getOptions());
                question.setCorrectAnswer(qDto.getCorrectAnswer());
                question.setScore(qDto.getScore());
                question.setKnowledgePoints(qDto.getKnowledgePoints());
                question.setDifficulty(qDto.getDifficulty());
                question.setExplanation(qDto.getExplanation());
                question.setAutoGradable(qDto.getAutoGradable() != null ? qDto.getAutoGradable() : isAutoGradable(QuestionType.valueOf(qDto.getType())));
                assignment.getQuestions().add(question);
            }
        }
        
        return assignmentRepository.save(assignment);
    }
}
