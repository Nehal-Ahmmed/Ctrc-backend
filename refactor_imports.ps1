$base = 'D:\flutter projects\CTRC system\Ctrc-backend\src\main\java\com\ctrc'
Get-ChildItem -Path $base -Recurse -Filter *.java | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    
    # Update package statements
    $content = $content -replace 'package com.ctrc.common;', 'package com.ctrc.core.presentation;'
    $content = $content -replace 'package com.ctrc.config;', 'package com.ctrc.core.config;'
    $content = $content -replace 'package com.ctrc.core.presentation;(.*)(ConflictException|ResourceNotFoundException|ValidationException)', 'package com.ctrc.core.domain.exceptions;$1$2'
    
    $content = $content -replace 'package com.ctrc.location;', 'package com.ctrc.location.domain;'
    $content = $content -replace 'package com.ctrc.location.domain;(.*)LocationRepositoryImpl', 'package com.ctrc.location.infrastructure;$1LocationRepositoryImpl'
    
    $content = $content -replace 'package com.ctrc.report;', 'package com.ctrc.report.domain;'
    $content = $content -replace 'package com.ctrc.report.domain;(.*)ReportRepositoryImpl', 'package com.ctrc.report.infrastructure;$1ReportRepositoryImpl'
    $content = $content -replace 'package com.ctrc.report.domain;(.*)ReportService', 'package com.ctrc.report.application;$1ReportService'
    $content = $content -replace 'package com.ctrc.report.domain;(.*)CreateReportRequest', 'package com.ctrc.report.application.dto;$1CreateReportRequest'
    $content = $content -replace 'package com.ctrc.report.domain;(.*)ReportController', 'package com.ctrc.report.presentation;$1ReportController'
    
    # Update imports
    $content = $content -replace 'import com.ctrc.common.ApiResponse;', 'import com.ctrc.core.presentation.ApiResponse;'
    $content = $content -replace 'import com.ctrc.common.ResourceNotFoundException;', 'import com.ctrc.core.domain.exceptions.ResourceNotFoundException;'
    $content = $content -replace 'import com.ctrc.common.ConflictException;', 'import com.ctrc.core.domain.exceptions.ConflictException;'
    $content = $content -replace 'import com.ctrc.common.ValidationException;', 'import com.ctrc.core.domain.exceptions.ValidationException;'
    
    $content = $content -replace 'import com.ctrc.location.Location;', 'import com.ctrc.location.domain.Location;'
    $content = $content -replace 'import com.ctrc.location.LocationDao;', 'import com.ctrc.location.domain.LocationRepository;'
    
    $content = $content -replace 'import com.ctrc.report.Report;', 'import com.ctrc.report.domain.Report;'
    $content = $content -replace 'import com.ctrc.report.ReportDao;', 'import com.ctrc.report.domain.ReportRepository;'
    $content = $content -replace 'import com.ctrc.report.CreateReportRequest;', 'import com.ctrc.report.application.dto.CreateReportRequest;'
    
    # Rename Dao to Repository in code
    $content = $content -replace 'LocationDao', 'LocationRepository'
    $content = $content -replace 'ReportDao', 'ReportRepository'
    
    Set-Content -Path $_.FullName -Value $content -NoNewline
}
