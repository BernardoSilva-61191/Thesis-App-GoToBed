"use strict";

// this is just a fake backend implementation for developing the app
// in the brower. none of the stuff is supposed to work or do any real
// work - just kicking up the app (dispatching deviceready-event) is ok.
$(function() {

    // no need for fake backend if backed is there
    if (
        window.SensorCalibration ||
        window.SensorReader ||
        window.SensorDiscovery
    ) return;

    function log(text) {
        console && console.log && console.log("[fake backend] " + text);
    }

    log("Faking native backend (running in a browser?)");
    var fakeItemTimestamp = 0;

    window.SensorCalibration = {
        queryCalibrationStatus: function() {
            setTimeout(function() {
                window.app.receiveCalibrationStatus("Fake calibration status!");
            }.bind(this), 5000);
        },
      sendCalibrationParameters: function(host, parameters) {
        console.log("Fake sending calibrationParameters. " + host + ": " + parameters);
        setTimeout(function() {
                window.app.sendCalibrationParametersCallback("parameters " + parameters + " sent.");
            }.bind(this), 5000);
        },
    };

  window.SensorCommand = {
    restore: function() {
      setTimeout(function() {
        console.log("Fake reset bcg config.");
      }.bind(this), 5000);
    }
  };

    window.SensorReader = {
        startPolling: function() {
            log("Begun polling (not actually doing anything)");
        },
        stopPolling: function() {
            log("Stopped polling (not actually doing anything)");
        },
        getItems: function() {
            log("Outputting dummy items");

            var now = new Date().getTime();

            var items = [
                (++fakeItemTimestamp), // timestamp
                61 + Math.round(Math.abs(Math.sin(now / 70000) * 30)), // heart rate
                Math.round(11 + Math.sin(now / 12000) * 2), // respiration rate
                Math.round(58 + Math.sin(now / 2000) * 2), // relative stroke volume
                Math.round(105 + Math.sin(now / 4800) * 4), // heart rate variability
                Math.round(7500 + Math.sin(now / 50) * 500), // measured signal strength
                Math.round(Math.sin(now / 60000) < 0.8 ? 1 : (Math.sin(now / 60000) < 0.95 ? 2 : 0)), // status
                Math.round(542 - Math.sin(now) * 40), // beat to beat time
                Math.round(603 - Math.sin(now / 7) * 30), // beat to beat time minus one
                0 // beat to beat time minus two
            ];

            return items.join(',');
        }
    };

    window.SensorDiscovery = {
        startDiscovery: function() {
            log("Discovery started (not actually doing anything)");
        },
        getDiscoveredSensors: function() {
            log("Discovert polled, returning ...");
            if (Math.random() < 0.25) {
                log("... fake empty results");
                return JSON.stringify([]);
            } else {
                log("... faked results");
                return JSON.stringify([{
                    ip: '1.2.3.4',
                    hostname: 'foo'
                }, {
                    ip: '192.168.192.168',
                    hostname: 'bar'
                }]);
            }
        }
    };

    window.SensorStorage = {
        getHistory: function(count) {
            log("Fake get latest " + count + " items from history (not actually doing anything) ");
        }
    };

  window.SensorRunningMode = {
        setBCGRunningMode: function() {
            log("fake set sensor running mode");
        }
    };
  
  document.dispatchEvent(new CustomEvent('deviceready'));

});
