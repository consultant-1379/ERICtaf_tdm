import javaposse.jobdsl.dsl.views.jobfilter.MatchType
import javaposse.jobdsl.dsl.views.jobfilter.Status

import static com.ericsson.de.taf.jenkins.dsl.Constants.JOBS_PREFIX
import static com.ericsson.de.taf.jenkins.dsl.Constants.PROJECT_NAME

listView(JOBS_PREFIX) {
    description "Jobs for ${PROJECT_NAME}"
    jobs {
        regex(/^${JOBS_PREFIX}.*$/)
    }
    jobFilters {
        status {
            matchType MatchType.EXCLUDE_MATCHED
            status Status.DISABLED
        }
    }
    columns {
        status()
        weather()
        name()
        lastDuration()
        lastSuccess()
        lastBuildConsole()
        buildButton()
        configureProject()
    }
}
