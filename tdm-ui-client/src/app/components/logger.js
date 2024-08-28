const _log = new WeakMap();
const _toastr = new WeakMap();

export default class Logger {

    constructor($log, toastr) {
        'ngInject';

        _log.set(this, $log);
        _toastr.set(this, toastr);
    }

    warn(msg, data) {
        _log.get(this).warn('Warning: ' + msg, data);
    }

    info(msg, data) {
        _log.get(this).info('Info: ' + msg, data);
    }

    success(msg, data) {
        _log.get(this).info('Success: ' + msg, data);
    }

    error(msg, data) {
        _log.get(this).error('Error: ' + msg, data);
    }

    warnWithToast(msg, data) {
        _log.get(this).warn('Warning: ' + msg, data);
        _toastr.get(this).warning(msg, '', {
            timeOut: 10000
        });
    }

    infoWithToast(msg, data) {
        _log.get(this).info('Info: ' + msg, data);
        _toastr.get(this).info(msg);
    }

    successWithToast(msg, data) {
        _log.get(this).info('Success: ' + msg, data);
        _toastr.get(this).success(msg);
    }

    errorWithToast(msg, data) {
        _log.get(this).error('Error: ' + msg, data);
        _toastr.get(this).error(msg, data, {
            timeOut: 10000
        });
    }

    clearToasts() {
        _toastr.get(this).clear();
    }
}
