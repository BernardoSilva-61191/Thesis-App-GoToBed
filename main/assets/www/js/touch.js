
function touch() {
}

touch.prototype.bindTouch = function(objects, eventHandler, timeout) {
    $(objects).on('touchstart click', function(event) {
        // prevent double clicks that may occur with some android devices
        if (
            this.clickGuard &&
            this.clickGuard.id === event.target.id &&
            (new Date().getTime() - this.clickGuard.timestamp) < (timeout || 1000)
        ) {
            event.stopPropagation();
            event.preventDefault();
            return false;
        }
        this.clickGuard = {
            id: event.target.id,
            timestamp: new Date().getTime()
        };

        event.stopPropagation();
        event.preventDefault();
        eventHandler(event);
        return false;
    });
};
