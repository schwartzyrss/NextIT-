package com.nextit;

import com.nextit.model.Project;
import com.nextit.service.ProjectService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectEvaluationTest {

    @Test
    void averageIgnoresMissingScores() {
        assertEquals(85.0, ProjectService.average(90.0, 80.0, null));
        assertEquals(87.67, ProjectService.average(90.0, 88.0, 85.0));
    }

    @Test
    void averageReturnsNullWhenNothingWasScored() {
        assertNull(ProjectService.average(null, null, null));
    }

    @Test
    void isOwnerOnlyMatchesTheRecordedOwner() {
        ProjectService service = new ProjectService();
        Project project = new Project();
        project.setStudentId(7);

        assertTrue(service.isOwner(project, 7));
        assertFalse(service.isOwner(project, 8));
        assertFalse(service.isOwner(project, 0));
    }

    @Test
    void isOwnerRejectsProjectsWithoutAnOwner() {
        ProjectService service = new ProjectService();
        Project project = new Project();

        assertFalse(service.isOwner(project, 7));
        assertFalse(service.isOwner(null, 7));
    }
}
