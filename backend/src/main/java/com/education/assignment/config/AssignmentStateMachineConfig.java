package com.education.assignment.config;

import com.education.assignment.enums.AssignmentEvent;
import com.education.assignment.enums.AssignmentStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class AssignmentStateMachineConfig extends EnumStateMachineConfigurerAdapter<AssignmentStatus, AssignmentEvent> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<AssignmentStatus, AssignmentEvent> config)
            throws Exception {
        config
            .withConfiguration()
            .autoStartup(true)
            .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<AssignmentStatus, AssignmentEvent> states)
            throws Exception {
        states
            .withStates()
            .initial(AssignmentStatus.DRAFT)
            .states(EnumSet.allOf(AssignmentStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AssignmentStatus, AssignmentEvent> transitions)
            throws Exception {
        transitions
            .withExternal()
                .source(AssignmentStatus.DRAFT).target(AssignmentStatus.PUBLISHED).event(AssignmentEvent.PUBLISH)
                .and()
            .withExternal()
                .source(AssignmentStatus.PUBLISHED).target(AssignmentStatus.OPEN).event(AssignmentEvent.OPEN)
                .and()
            .withExternal()
                .source(AssignmentStatus.OPEN).target(AssignmentStatus.SUBMITTED).event(AssignmentEvent.SUBMIT)
                .and()
            .withExternal()
                .source(AssignmentStatus.SUBMITTED).target(AssignmentStatus.AUTO_GRADING).event(AssignmentEvent.START_AUTO_GRADE)
                .and()
            .withExternal()
                .source(AssignmentStatus.AUTO_GRADING).target(AssignmentStatus.AUTO_GRADED).event(AssignmentEvent.COMPLETE_AUTO_GRADE)
                .and()
            .withExternal()
                .source(AssignmentStatus.AUTO_GRADED).target(AssignmentStatus.MANUAL_GRADING).event(AssignmentEvent.START_MANUAL_GRADE)
                .and()
            .withExternal()
                .source(AssignmentStatus.AUTO_GRADED).target(AssignmentStatus.GRADED).event(AssignmentEvent.COMPLETE_MANUAL_GRADE)
                .and()
            .withExternal()
                .source(AssignmentStatus.MANUAL_GRADING).target(AssignmentStatus.GRADED).event(AssignmentEvent.COMPLETE_MANUAL_GRADE)
                .and()
            .withExternal()
                .source(AssignmentStatus.GRADED).target(AssignmentStatus.RETURNED).event(AssignmentEvent.RETURN)
                .and()
            .withExternal()
                .source(AssignmentStatus.RETURNED).target(AssignmentStatus.ARCHIVED).event(AssignmentEvent.ARCHIVE);
    }

    @Bean
    public StateMachineListener<AssignmentStatus, AssignmentEvent> listener() {
        return new StateMachineListenerAdapter<AssignmentStatus, AssignmentEvent>() {
            @Override
            public void stateChanged(State<AssignmentStatus, AssignmentEvent> from, State<AssignmentStatus, AssignmentEvent> to) {
                System.out.println("State changed from: " + (from != null ? from.getId() : "null") + " to: " + to.getId());
            }
        };
    }
}
