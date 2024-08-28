package com.ericsson.cifwk.tdm.presentation.exceptions;

import com.ericsson.cifwk.tdm.model.Lock;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         20/06/2017
 */
@ResponseStatus(value = HttpStatus.LOCKED, reason = "Resource locked")
public class LockedException extends RuntimeException {

    private final String executionId;

    public LockedException(Lock lock) {
        this.executionId = lock.getDataSourceExecution().getExecutionId();
    }

    @Override
    public String getMessage() {
        return "Data source is locked by execution Id: " + executionId;
    }
}
