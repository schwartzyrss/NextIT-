package com.nextit.service;

import com.nextit.model.Portfolio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortfolioVisibilityTest {

    @Test
    void acceptsEveryDocumentedVisibilityState() {
        assertTrue(PortfolioService.isValidVisibility("Private"));
        assertTrue(PortfolioService.isValidVisibility("Pending"));
        assertTrue(PortfolioService.isValidVisibility("Approved"));
        assertTrue(PortfolioService.isValidVisibility("Public"));
    }

    @Test
    void rejectsUnknownOrMissingVisibility() {
        assertFalse(PortfolioService.isValidVisibility("Shared"));
        assertFalse(PortfolioService.isValidVisibility(""));
        assertFalse(PortfolioService.isValidVisibility(null));
    }

    @Test
    void recognisesPublicAndApprovedPortfolios() {
        assertTrue(PortfolioService.isPublic(portfolio("Public")));
        assertTrue(PortfolioService.isApproved(portfolio("Approved")));
        assertFalse(PortfolioService.isApproved(portfolio("Public")));
        assertFalse(PortfolioService.isPublic(portfolio("Approved")));
        assertFalse(PortfolioService.isPublic(portfolio("Private")));
        assertFalse(PortfolioService.isPublic(null));
    }

    private static Portfolio portfolio(String visibility) {
        Portfolio portfolio = new Portfolio();
        portfolio.setVisibility(visibility);
        return portfolio;
    }
}
