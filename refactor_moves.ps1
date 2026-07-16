$base = 'D:\flutter projects\CTRC system\Ctrc-backend\src\main\java\com\ctrc'

New-Item -ItemType Directory -Force -Path "$base\core\presentation"
New-Item -ItemType Directory -Force -Path "$base\core\domain\exceptions"
New-Item -ItemType Directory -Force -Path "$base\core\config"
New-Item -ItemType Directory -Force -Path "$base\location\domain"
New-Item -ItemType Directory -Force -Path "$base\location\infrastructure"
New-Item -ItemType Directory -Force -Path "$base\report\domain"
New-Item -ItemType Directory -Force -Path "$base\report\application\dto"
New-Item -ItemType Directory -Force -Path "$base\report\application"
New-Item -ItemType Directory -Force -Path "$base\report\infrastructure"
New-Item -ItemType Directory -Force -Path "$base\report\presentation"

Move-Item "$base\common\ApiResponse.java" "$base\core\presentation"
Move-Item "$base\common\GlobalExceptionHandler.java" "$base\core\presentation"
Move-Item "$base\common\HealthController.java" "$base\core\presentation"
Move-Item "$base\common\ConflictException.java" "$base\core\domain\exceptions"
Move-Item "$base\common\ResourceNotFoundException.java" "$base\core\domain\exceptions"
Move-Item "$base\common\ValidationException.java" "$base\core\domain\exceptions"
Move-Item "$base\config\CorsConfig.java" "$base\core\config"

Move-Item "$base\location\Location.java" "$base\location\domain"
Move-Item "$base\location\LocationDao.java" "$base\location\domain\LocationRepository.java"
Move-Item "$base\location\LocationDaoImpl.java" "$base\location\infrastructure\LocationRepositoryImpl.java"

Move-Item "$base\report\CreateReportRequest.java" "$base\report\application\dto"
Move-Item "$base\report\Report.java" "$base\report\domain"
Move-Item "$base\report\ReportController.java" "$base\report\presentation"
Move-Item "$base\report\ReportDao.java" "$base\report\domain\ReportRepository.java"
Move-Item "$base\report\ReportDaoImpl.java" "$base\report\infrastructure\ReportRepositoryImpl.java"
Move-Item "$base\report\ReportService.java" "$base\report\application"

Remove-Item "$base\common" -Recurse -Force
Remove-Item "$base\config" -Recurse -Force
