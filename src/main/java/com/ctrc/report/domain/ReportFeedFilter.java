package com.ctrc.report.domain;

import com.ctrc.core.domain.exceptions.ValidationException;

import java.util.Set;

public class ReportFeedFilter {

    public static final String SORT_NEAREST = "nearest";
    public static final String SORT_NEWEST = "newest";
    public static final String SORT_OLDEST = "oldest";
    public static final String SORT_TOP = "top";
    public static final String SORT_DISCUSSED = "discussed";
    public static final String SORT_CONFIRMED = "confirmed";

    private static final Set<String> SORTS = Set.of(
            SORT_NEAREST, SORT_NEWEST, SORT_OLDEST, SORT_TOP, SORT_DISCUSSED, SORT_CONFIRMED);

    private static final Set<String> STATUSES = Set.of("unverified", "verified", "disputed");

    private static final Set<String> EVIDENCE_TYPES = Set.of("seen", "heard", "guessed");

    private static final int MAX_WITHIN_HOURS = 24 * 30;

    public static final ReportFeedFilter DEFAULT =
            new ReportFeedFilter(SORT_NEAREST, null, null, null, false);

    private final String sort;
    private final String status;
    private final String evidenceType;
    private final Integer withinHours;
    private final boolean withPhotoOnly;

    private ReportFeedFilter(String sort, String status, String evidenceType,
                             Integer withinHours, boolean withPhotoOnly) {
        this.sort = sort;
        this.status = status;
        this.evidenceType = evidenceType;
        this.withinHours = withinHours;
        this.withPhotoOnly = withPhotoOnly;
    }

    public static ReportFeedFilter of(String sort, String status, String evidenceType,
                                      Integer withinHours, Boolean withPhotoOnly) {
        String normalisedSort = normalise(sort);
        if (normalisedSort == null) {
            normalisedSort = SORT_NEAREST;
        } else if (!SORTS.contains(normalisedSort)) {
            throw new ValidationException("unknown sort: " + sort);
        }

        String normalisedStatus = normalise(status);
        if (normalisedStatus != null && !STATUSES.contains(normalisedStatus)) {
            throw new ValidationException("unknown status: " + status);
        }

        String normalisedEvidence = normalise(evidenceType);
        if (normalisedEvidence != null && !EVIDENCE_TYPES.contains(normalisedEvidence)) {
            throw new ValidationException("unknown evidence type: " + evidenceType);
        }

        if (withinHours != null && (withinHours < 1 || withinHours > MAX_WITHIN_HOURS)) {
            throw new ValidationException("withinHours must be between 1 and " + MAX_WITHIN_HOURS);
        }

        return new ReportFeedFilter(normalisedSort, normalisedStatus, normalisedEvidence,
                withinHours, Boolean.TRUE.equals(withPhotoOnly));
    }

    private static String normalise(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim().toLowerCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getSort() {
        return sort;
    }

    public String getStatus() {
        return status;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public Integer getWithinHours() {
        return withinHours;
    }

    public boolean isWithPhotoOnly() {
        return withPhotoOnly;
    }
}
