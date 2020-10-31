"use strict";

function bcgapp() {

  $('#calibrationTimeLeft').fadeOut();

    this.appStartTime = new Date().getTime();
    this.previousViewport = {
        width: 0,
        height: 0
    };

    $('#millisecondsAgo').val(60000);

    this.fetchedHistory = [];

    this.polling = false;
    this.emptyReceivedDataCount = 0;

    this.discoveryInProgress = false;

    this.saveSettingsDebounced = _.debounce(this.saveSettings.bind(this), 1000);

    this.calibrationPollerInterval = null;

    this.graphUpdateInterval = null;

  this.settings = {
    tab: localStorage.tab || 'settings',
    mode: localStorage.mode || 'offline',
    sourceAddress: localStorage.sourceAddress || 'Not set',

    exitAdaptiveCalibration: localStorage.exitAdaptiveCalibration === 'true' || true,
    showAdaptiveCalibrationParameters: localStorage.showAdaptiveCalibrationParameters === 'true' || false,

    requiredOccupiedCalibration:  parseInt(localStorage.requiredOccupiedCalibration) || 45,
    requiredEmptyCalibration:  parseInt(localStorage.requiredEmptyCalibration) || 15,
    calibrationDiscardLength:  parseInt(localStorage.calibrationDiscardLength) || 5,
    calibrationExp:  parseInt(localStorage.calibrationExp) || 0.5,
    calibrationEmptyMax:  parseInt(localStorage.calibrationEmptyMax) || 800,
    calibrationStdevMax:  parseInt(localStorage.calibrationStdevMax) || 400,
    calibrationGoodLimit:  parseInt(localStorage.calibrationGoodLimit) || 150,
    calibrationOkLimit:  parseInt(localStorage.calibrationOkLimit) || 400,

    oobProbabilityLpAlpha: parseFloat(localStorage.oobProbabilityLpAlpha) || 0.5,
    oobQualityLpAlpha: parseFloat(localStorage.oobQualityLpAlpha) || 0.5,
    oobMovementLpAlpha: parseInt(localStorage.oobMovementLpAlpha) || 1,
    oobAfterMovementFromMid: parseInt(localStorage.oobAfterMovementFromMid) || 25,
    oobWaitTimeAfterMovement: parseInt(localStorage.oobWaitTimeAfterMovement) || 3,
    oobProbabilityLimitChangeToEmpty: parseInt(localStorage.oobProbabilityLimitChangeToEmpty) || 30,
    oobProbabilityLimitChangeToOccupied: parseInt(localStorage.oobProbabilityLimitChangeToOccupied) || 40,

    calibProbabilityLpAlpha: parseFloat(localStorage.calibProbabilityLpAlpha) || 0.25,
    calibQualityLpAlpha: parseFloat(localStorage.calibQualityLpAlpha) || 0.5,
    calibMovementLpAlpha: parseInt(localStorage.calibMovementLpAlpha) || 1,
    calibAfterMovementFromMid: parseInt(localStorage.calibAfterMovementFromMid) || 25,
    calibWaitTimeAfterMovement: parseInt(localStorage.calibWaitTimeAfterMovement) || 4,
    calibProbabilityLimitChangeToEmpty: parseInt(localStorage.calibProbabilityLimitChangeToEmpty) || 10,
    calibProbabilityLimitChangeToOccupied: parseInt(localStorage.calibProbabilityLimitChangeToOccupied) || 70

  };
  this.initCalibration();

  // graphss
  this.graphs = new graphs("hr");
  this.touch = new touch();
  this.inbed = null;
  this.inbedBedStatusConfig();
  this.touch.bindTouch(
    '#menu .menuButton',
    function(e) {
      this.changeTab(e.currentTarget.id.replace("tab_", ""));
    }.bind(this)
  );

    this.touch.bindTouch(
      '.datasetbutton.primary',
      function(e) {
        $('.datasetbutton.primary').removeClass('selected');
        $(e.currentTarget).addClass('selected');
        this.graphs.setDataset($(e.currentTarget).attr('dataset'));
      }.bind(this)
    );

    this.touch.bindTouch(
      '.datasetbutton.secondary',
      function(e) {
        $('.datasetbutton.secondary').removeClass('selected');
        if (this.graphs.setSecondaryDataset($(e.currentTarget).attr('dataset'))) {
          $(e.currentTarget).addClass('selected');
        }
      }.bind(this)
    );

    this.touch.bindTouch(
        '#content_analysis .button',
        function(e) {
        var id = null;
          if (e.currentTarget && e.currentTarget.id) {
              id = e.currentTarget.id;
          }

            if (id === "settingsSleepAnalysis") {
               window.SensorSleepAnalysis.startSleepAnalysisActivity();
            }
        }.bind(this)
    );

    this.touch.bindTouch(
      '.analysis.button',
      function(e) {
        var dataset = $(e.currentTarget).attr('data-set');
        console && console.log && console.log("Show dataset " + dataset);
        $('.analysis.button').addClass('dimmed');

        $('.analysisValues .status img').attr('src', 'img/recovery/' + dataset + '_status.png');
        $(e.currentTarget).removeClass('dimmed');
      }.bind(this)
    );

  // settings - change ip address
  $('#content_settings #sensorIp').keyup(function(event) {
    event.stopPropagation();
    event.preventDefault();

    this.settings.sourceAddress = $('#content_settings #sensorIp').val() || '';
    this.saveSettingsDebounced();

    window.SensorRunningMode.setBCGRunningMode(this.settings.sourceAddress);

    this.autoconnect();

    return false;
  }.bind(this));

    this.touch.bindTouch(
      '#resetBedStatus',
      function() {
        this.inbedBedStatusConfig();
      }.bind(this)
    );
    this.touch.bindTouch(
      '#resetBedStatusConfig',
      function() {
        this.resetInbedBedStatusConfig();
      }.bind(this)
    );
    this.touch.bindTouch(
      '#sendCalibrationParameters',
      function() {
        var parameters = $("#calibrationParameters").val().split(/\W*,\W*/);
        this.sendCalibrationParameters(parameters);
      }.bind(this)
    );
    this.touch.bindTouch(
      '#adaptiveCalibrationButton',
      function(e) {
        if (this.settings.mode === "online") {
          this.startCalibration();
        }
      }.bind(this)
    );
    this.touch.bindTouch(
      '#adaptiveCalibrationWizard',
      function(e) {
        //capture events
      }.bind(this)
    );
    this.touch.bindTouch(
      '.emptyBedCalibrationButton',
      function(e) {
        this.startEmptyCalibration();
      }.bind(this)
    );
    this.touch.bindTouch(
      '.occupiedBedCalibrationButton',
      function(e) {
        this.finishEmptyCalibration();
      }.bind(this)
    );
    this.touch.bindTouch(
      '#adaptiveCalibrationOverlay',
      function(e) {
        this.endCalibration();
      }.bind(this)
    );
  this.touch.bindTouch(
    '#adaptiveCalibrationWizard .endCalibration',
    function(e) {
      this.endCalibration();
    }.bind(this)
  );
  this.touch.bindTouch(
    '#adaptiveCalibrationConfig',
    function(e) {
      $('#adaptiveCalibrationConfigOverlay').css('display', 'block');
    }.bind(this)
  );
  this.touch.bindTouch(
    '#adaptiveCalibrationConfigOverlay',
    function(e) {
      this.endConfig();
    }.bind(this)
  );
  $('.view').on('touchstart click', function(e) {
    e.stopPropagation();
  });

  this.touch.bindTouch(
    '#adaptiveCalibrationConfigView .doneConfig',
    function(e) {
      $('#adaptiveCalibrationConfigOverlay').css('display', 'none');
    }.bind(this)
  );

  this.touch.bindTouch(
    '#adaptiveCalibrationConfigView .resetCalibConfig',
    function(e) {
      console && console.log && console.log("Reset calibration config to defaults.");
      this.settings.requiredOccupiedCalibration = 45,
      this.settings.requiredEmptyCalibration = 15,
      this.settings.calibrationDiscardLength =  5,
      this.settings.calibrationExp = 0.5,
      this.settings.calibrationEmptyMax = 800,
      this.settings.calibrationStdevMax = 400,
      this.settings.calibrationGoodLimit = 150,
      this.settings.calibrationOkLimit = 400,
      this.saveSettings();
      this.drawSettings();
    }.bind(this)
  );
  this.touch.bindTouch(
    '#adaptiveCalibrationConfigView .resetOobConfig',
    function(e) {
      console && console.log && console.log("Reset bed status config to defaults.");
      this.settings.calibProbabilityLpAlpha = 0.5;
      this.settings.calibQualityLpAlpha = 0.5;
      this.settings.calibMovementLpAlpha = 1;
      this.settings.calibAfterMovementFromMid = 25;
      this.settings.calibWaitTimeAfterMovement = 3;
      this.settings.calibProbabilityLimitChangeToEmpty = 30;
      this.settings.calibProbabilityLimitChangeToOccupied = 40;
      this.saveSettings();
      this.drawSettings();
    }.bind(this)
  );
  this.touch.bindTouch(
    '#adaptiveCalibrationConfigView .tab',
    function(e) {
      $('#adaptiveCalibrationConfigView .tab').removeClass('selected');
      $(e.currentTarget).addClass('selected');
      $('#adaptiveCalibrationConfigView .tabTarget').removeClass('selected');
      var target = $(e.currentTarget).attr('data-target');
      $('#adaptiveCalibrationConfigView .tabTarget.' + target).addClass('selected');
    }.bind(this)
  );

  this.touch.bindTouch(
    '#exitAdaptiveCalibration, #showAdaptiveCalibrationParameters',
    function(e) {
      var status = !$(e.currentTarget).prop("checked");
      $(e.currentTarget).prop("checked", status);
      this.settings[e.currentTarget.id] = status;
      this.saveSettingsDebounced();
    }.bind(this), 10);
  $('#requiredOccupiedCalibration, #requiredEmptyCalibration, #calibrationDiscardLength, #calibrationEmptyMax, #calibrationStdevMax, #calibrationGoodLimit, #calibrationOkLimit, ' +
    '#oobMovementLpAlpha, #oobAfterMovementFromMid, #oobWaitTimeAfterMovement, #oobProbabilityLimitChangeToEmpty, #oobProbabilityLimitChangeToOccupied, ' +
    '#calibMovementLpAlpha, #calibAfterMovementFromMid, #calibWaitTimeAfterMovement, #calibProbabilityLimitChangeToEmpty, #calibProbabilityLimitChangeToOccupied')
    .keyup(
      function(e) {
        var value = e.currentTarget.value;
        this.settings[e.currentTarget.id] = parseInt(value);
        this.saveSettingsDebounced();
        return false;
      }.bind(this));

  $('#calibrationExp, #oobProbabilityLpAlpha, #oobQualityLpAlpha, #calibProbabilityLpAlpha, #calibQualityLpAlpha')
    .keyup(
      function(e) {
        var value = e.currentTarget.value;
        this.settings[e.currentTarget.id] = parseFloat(value);
        this.saveSettingsDebounced();
        return false;
      }.bind(this));
  this.touch.bindTouch('#calibrationFinished .close',
    function(e) {
      $('#calibrationFinished').fadeOut();
    }.bind(this)
  );
  this.touch.bindTouch(
    '.adaptiveCalibrationHelp',
    function(e) {
      $('#adaptiveCalibrationHelpOverlay').css('display', 'block');
    }.bind(this)
  );
  this.touch.bindTouch(
    '#adaptiveCalibrationHelpOverlay',
    function(e) {
      $('#adaptiveCalibrationHelpOverlay').css('display', 'none');
    }.bind(this)
  );
  this.touch.bindTouch(
    '#adaptiveCalibrationHelpView .done',
    function(e) {
      $('#adaptiveCalibrationHelpOverlay').css('display', 'none');
    }.bind(this)
  );
  this.touch.bindTouch(
    '#bedStatusConfigurationsButton',
    function(e) {
      $('#bedStatusConfigurationsOverlay').css('display', 'block');
    }.bind(this)
  );
  this.touch.bindTouch(
    '#bedStatusConfigurationsOverlay',
    function(e) {
      $('#bedStatusConfigurationsOverlay').css('display', 'none');
    }.bind(this)
  );
  this.touch.bindTouch(
    '#bedStatusConfigurations .doneConfig',
    function(e) {
      $('#bedStatusConfigurationsOverlay').css('display', 'none');
    }.bind(this)
  );

  this.touch.bindTouch(
    '#content_settings .button',
    function(e) {
      var id = null;
            if (e.currentTarget && e.currentTarget.id) {
                id = e.currentTarget.id;
            }

            // sensor discovery
            if (id === "autoDiscovery" && !this.discoveryInProgress && confirm(
                    "Attempt to auto-discover a sensor from current network?"
                )) {

                this.discoveryInProgress = true;
                window.SensorDiscovery.startDiscovery();
                $('#autoDiscovery').html(
                    '<i class="fa fa-circle-o-notch fa-spin"></i> ' +
                    'Searching ... '
                );

                setTimeout(function() {
                    this.discoveryInProgress = false;
                    $('#autoDiscovery').html('Find sensors');
                  ;
                    var sensors = JSON.parse(window.SensorDiscovery.getDiscoveredSensors());

                    if (sensors.length === 0) {
                        alert(
                            'Could not find any sensors!\n' +
                            'Are you connected to the same network?'
                        );
                        return;
                    }

                    var foundSensorsContainer = $('#foundSensors');
                    foundSensorsContainer.empty();
                    _.each(sensors, function addSensorButton(sensor) {
                        var useSensor = function() {

                            this.settings.sourceAddress = sensor.ip;
                            this.settings.mode = "online";
                            this.saveSettingsDebounced();
                            this.drawSettings();

                            this.autoconnect();

                            alert('Now using sensor ' + sensor.ip);
                        };
                        var button = $(
                            '<a class="button">' +
                            sensor.ip + ' ' + sensor.hostname +
                            '</a>'
                        );
                        button.click(useSensor.bind(this));
                        foundSensorsContainer.append(button);
                    }.bind(this));

                }.bind(this), 1000 * 10);
            }

            // online / offline
            if (id === "settingsOffline") {
                this.settings.mode = "offline";
                this.saveSettingsDebounced();

                $('#connectionAlert:visible').fadeOut();
            }

            if (id === "settingsOnline") {
                this.settings.mode = "online";
                this.saveSettingsDebounced();

                this.emptyReceivedDataCount = 0;
            }

            if (id === "settingsAlarm") {
                window.SensorAlarm.startAlarmActivity();
            }

            this.drawSettings();
        }.bind(this)
    );

}


bcgapp.prototype.inbedBedStatusConfig = function() {
  if (this.inbed) {
    this.inbedFree(this.inbed);
  }
  this.inbed = this.inbedInit();
  console && console.log && console.log("Reset bed status with: " +
                                        this.settings.oobProbabilityLpAlpha,
                                        this.settings.oobQualityLpAlpha,
                                        this.settings.oobMovementLpAlpha,
                                        this.settings.oobAfterMovementFromMid,
                                        this.settings.oobWaitTimeAfterMovement,
                                        this.settings.oobProbabilityLimitChangeToEmpty,
                                        this.settings.oobProbabilityLimitChangeToOccupied);
  this.inbedConfig(this.inbed.config,
                   this.settings.oobProbabilityLpAlpha,
                   this.settings.oobQualityLpAlpha,
                   this.settings.oobMovementLpAlpha,
                   this.settings.oobAfterMovementFromMid,
                   this.settings.oobWaitTimeAfterMovement,
                   this.settings.oobProbabilityLimitChangeToEmpty,
                   this.settings.oobProbabilityLimitChangeToOccupied);
};

bcgapp.prototype.resetInbedBedStatusConfig = function() {
  console && console.log && console.log("Reset bed status config to defaults.");
  this.settings.oobProbabilityLpAlpha = 0.25;
  this.settings.oobQualityLpAlpha = 0.5;
  this.settings.oobMovementLpAlpha = 1;
  this.settings.oobAfterMovementFromMid = 25;
  this.settings.oobWaitTimeAfterMovement = 4;
  this.settings.oobProbabilityLimitChangeToEmpty = 10;
  this.settings.oobProbabilityLimitChangeToOccupied = 70;

  this.saveSettingsDebounced();
  this.drawSettings();
  this.inbedBedStatusConfig();
};

bcgapp.prototype.updateCalibrationView = function() {
  if (this.calibration.status === 'discard') {
    $('#remainingDiscardCalibration').text(
      Math.max(this.calibration.config.discardLength -
               this.calibration.discardCount, 0));
  } else if (this.calibration.status === 'empty') {
    $('#remainingEmptyCalibration').text(
      Math.max(this.calibration.config.requiredEmptyCalibration -
               this.calibration.emptyLog.length, 0));
    if (this.calibration.emptyLog.length === this.calibration.config.requiredEmptyCalibration) {
      this.finishEmptyCalibrationLoop();
    }
  } else if (this.calibration.status === 'occupied') {
    var occupiedIndex = this.calibration.occupiedSSLog.length - 1;
    $('#remainingOccupiedCalibration').text(
      Math.max(this.calibration.config.requiredOccupiedCalibration -
               this.calibration.occupiedSSLog[occupiedIndex].length, 0));
    $('#occupiedCalibrationLoop').text(this.calibration.occupiedSSLog.length);
    if (this.calibration.occupiedSSLog[occupiedIndex].length ===
        this.calibration.config.requiredOccupiedCalibration) {
      this.finishOccupiedCalibrationLoop();
    }
  }
};

bcgapp.prototype.updateCalibrationData = function() {
  if (this.fetchedHistory && this.fetchedHistory.length > 0) {
    var dataRow = this.fetchedHistory[this.fetchedHistory.length - 1];
    console && console.log &&
      console.log("Calibration data: " + dataRow);
    switch (this.calibration.status) {
    case 'discard':
      this.calibration.discardCount++;
      if (this.calibration.discardCount === this.calibration.config.discardLength) {
        this.calibration.discardCallback();
      }
      break;
    case 'empty':
      this.calibration.emptyLog.push(dataRow[5]); // signal strength
      break;
    case 'occupied':
      this.inbedAnalyze(this.fetchedHistory, this.calibration.inbed);
      var inbed = this.getInbed(this.calibration.inbed);
      if (dataRow[6] !== 2) {
        if (inbed.inbed === 1) {
          $('#occupiedCalibrationAlert').css('display', 'none');
          var occupiedIndex = this.calibration.occupiedSSLog.length - 1;
          this.calibration.occupiedSSLog[occupiedIndex].push(dataRow[5]);  // signal strength
          this.calibration.occupiedSVLog[occupiedIndex].push(dataRow[3]); // stroke volume
        } else if (inbed.inbed === 0) {
          this.calibration.emptyLog.push(dataRow[5]); // signal strength
          console && console.log &&
            console.log("Empty bed calibration data: Bed calibration status " + inbed.inbed +
                        ", " + inbed.inbedProbability);
          $('#occupiedCalibrationAlert').css('display', 'block').find("p")
            .text("Bed is empty, please occupy the bed to continue calibration.");
        } else {
          console && console.log &&
            console.log("Calibration data discarded: Bed calibration status " + inbed.inbed);
          $('#occupiedCalibrationAlert').css('display', 'block').find("p")
            .text("Movement, data discarded.");
        }
      } else {
        console && console.log &&
          console.log("Calibration data discarded: BCG status " + dataRow[6]);
        $('#occupiedCalibrationAlert').css('display', 'block').find("p")
          .text("Movement, data discarded.");
      }
      break;
    }
  }
};

bcgapp.prototype.setCalibrationStatus = function(state, callback) {
  if (state === 'discard') {
    this.calibration.discardCallback = callback.bind(this);
    this.calibration.discardCount = 0;
  }
  this.calibration.status = state;
};

bcgapp.prototype.initCalibration = function() {
  this.calibration = {
    status: null,
    occupiedSSLog: null,
    occupiedSVLog: null,
    oldParameters: null,
    config: this.getCalibrationConfig(),
    emptyLog: null,
    inbed: null
  };
};

bcgapp.prototype.resetCalibration = function() {
  if (this.calibration.inbed) {
    this.inbedFree(this.calibration.inbed);
  }
  this.calibration.inbed = this.inbedInit();

  this.calibration.status = null;
  this.calibration.occupiedSSLog = null;
  this.calibration.occupiedSVLog = null;
  this.calibration.oldParameters = null;
  this.calibration.config = this.getCalibrationConfig();
  this.calibration.emptyLog =  null;
  this.inbedConfig(this.calibration.inbed.config,
                   this.settings.calibProbabilityLpAlpha,
                   this.settings.calibQualityLpAlpha,
                   this.settings.calibMovementLpAlpha,
                   this.settings.calibAfterMovementFromMid,
                   this.settings.calibWaitTimeAfterMovement,
                   this.settings.calibProbabilityLimitChangeToEmpty,
                   this.settings.calibProbabilityLimitChangeToOccupied);
};

bcgapp.prototype.getCalibrationConfig = function() {
  return {
    requiredOccupiedCalibration: this.settings.requiredOccupiedCalibration,
    requiredEmptyCalibration: this.settings.requiredEmptyCalibration,
    emptyMax: this.settings.calibrationEmptyMax,
    stdevMax: this.settings.calibrationStdevMax,
    exp: this.settings.calibrationExp,
    discardLength: this.settings.calibrationDiscardLength,
    goodCalibLimit: this.settings.calibrationGoodLimit,
    okCalibLimit: this.settings.calibrationOkLimit
  };
};

bcgapp.prototype.startCalibration = function() {
  $('#adaptiveCalibrationOverlay').css('display', 'block');
  $('#adaptiveCalibrationWizard .phase').css('display', 'none')
    .filter('#startCalibration').css('display', 'block');
  $('#calibrationQuality').css('display', 'none').find("p").removeClass().text("");
  $('#sentCalibrationParameters').css('display', 'none').find(".value").text("");
  this.resetCalibration();
};

bcgapp.prototype.startEmptyCalibration = function() {
  $('#remainingEmptyCalibration').text(this.calibration.config.requiredEmptyCalibration);
  $('#emptyCalibrationHelp').css('display', 'block');
  $('#emptyCalibrationAlert').css('display', 'none');
  $('#adaptiveCalibrationWizard .phase').css('display', 'none')
    .filter('#emptyBedCalibration').css('display', 'block');
  this.calibration.emptyLog = [];
  this.setCalibrationStatus('empty');
};

bcgapp.prototype.finishEmptyCalibrationLoop = function() {
  this.setCalibrationStatus(null);
  var adaptiveResult = window.adaptive(null, null, this.calibration.emptyLog);
  var stdev = window.mat.std(this.calibration.emptyLog);
  if (adaptiveResult.emptyLevel > this.calibration.config.emptyMax ||
      stdev > this.calibration.config.stdevMax) {
    $('#emptyCalibrationHelp').css('display', 'none');
    $('#emptyCalibrationAlert').css('display', 'block');
  } else {
    this.finishEmptyCalibration();
  }
};

bcgapp.prototype.finishEmptyCalibration = function() {
  var adaptiveResult = window.adaptive(null, null, this.calibration.emptyLog);
  var parameters = [7000, window.mat.round(adaptiveResult.emptyLevel * 1.25), 2600, 2600, 1000, 7];
  this.calibration.oldParameters = [parameters];
  this.sendCalibrationParameters(parameters);
  this.startDiscardCalibration('empty', this.startOccupiedCalibration);
};


bcgapp.prototype.startDiscardCalibration = function(phase, callback) {
  $('#remainingDiscardCalibration').text(this.calibration.config.discardLength);
  $('#adaptiveCalibrationWizard .phase').css('display', 'none')
    .filter('#discardCalibration' + phase).css('display', 'block');
  this.setCalibrationStatus("discard", callback);
};

bcgapp.prototype.startOccupiedCalibration = function() {
  $('#remainingOccupiedCalibration').text(this.calibration.config.requiredOccupiedCalibration);
  $('#occupiedCalibrationHelp').css('display', 'block');
  $('#occupiedCalibrationAlert').css('display', 'none');
  $('#adaptiveCalibrationWizard .phase').css('display', 'none')
    .filter('#occupiedBedCalibration').css('display', 'block');
  this.calibration.occupiedSSLog = [[]];
  this.calibration.occupiedSVLog = [[]];
  this.setCalibrationStatus('occupied');
};

bcgapp.prototype.finishOccupiedCalibrationLoop = function() {
  var occupiedIndex = this.calibration.occupiedSSLog.length - 1;
  var occupiedSSLog;
  var occupiedSVLog;
  if (occupiedIndex > 0) {
    occupiedSSLog = this.calibration.occupiedSSLog[occupiedIndex - 1].concat(
      this.calibration.occupiedSSLog[occupiedIndex -1]
    );
    occupiedSVLog = this.calibration.occupiedSVLog[occupiedIndex];
  } else {
    occupiedSSLog = this.calibration.occupiedSSLog[occupiedIndex];
    occupiedSVLog = null;
  }
  var adaptiveResult = window.adaptive(occupiedSSLog, occupiedSVLog, this.calibration.emptyLog);
  var oldParametersIndex = this.calibration.oldParameters.length - 1;
  var parameters;
  if (occupiedIndex > 0) {
    parameters = window.mat.round(window.mat.add(
      window.mat.multiply(this.calibration.oldParameters[oldParametersIndex],
                          1 - this.calibration.config.exp),
      window.mat.multiply(adaptiveResult.parameters, this.calibration.config.exp)));
  } else {
    parameters = adaptiveResult.parameters;
  }
  var parsDiff = window.mat.abs(
    window.mat.subtract(parameters, this.calibration.oldParameters[oldParametersIndex]));
  var exitCalibration = false;
  if (window.mat.lt(parsDiff.slice(2,5), this.calibration.config.goodCalibLimit)) {
    $('#calibrationQuality').css('display', 'block').find("p")
      .text("Calibration quality good! You can now exit calibration.").removeClass().addClass("good");
    console && console.log && console.log("Calibration quality good!");
    if (this.settings.exitAdaptiveCalibration) {
      exitCalibration = true;
    }
  } else if (window.mat.lt(parsDiff.slice(2,5), this.calibration.config.okCalibLimit)) {
    $('#calibrationQuality').css('display', 'block').find("p")
      .text("Calibration quality OK! Please continue calibration for optimal performance!").removeClass().addClass("ok");
    console && console.log && console.log("Calibration quality OK!");
  } else {
    $('#calibrationQuality').css('display', 'block').find("p")
      .text("Calibration quality bad! Please continue iterations!").removeClass().addClass("bad");
    console && console.log && console.log("Calibration quality bad!");
  }
  this.sendCalibrationParameters(parameters);
  this.calibration.oldParameters.push(parameters);
  if (exitCalibration) {
    $('#calibrationFinished').fadeIn();
    setTimeout(function() {
      $('#calibrationFinished').fadeOut();
    }, 30000);
    this.endCalibration();
  } else {
    this.startDiscardCalibration('occupied', this.continueOccupiedCalibration);
  }
};

bcgapp.prototype.continueOccupiedCalibration = function() {
  $('#remainingOccupiedCalibration').text(this.calibration.config.requiredOccupiedCalibration);
  $('#occupiedCalibrationHelp').css('display', 'block');
  $('#occupiedCalibrationAlert').css('display', 'none');
  $('#adaptiveCalibrationWizard .phase').css('display', 'none')
    .filter('#occupiedBedCalibration').css('display', 'block');
  this.calibration.occupiedSSLog.push([]);
  this.calibration.occupiedSVLog.push([]);
  this.setCalibrationStatus('occupied');
};

bcgapp.prototype.endCalibration = function() {
  $('#adaptiveCalibrationOverlay').css('display', 'none');
  this.drawSettings();
  this.setCalibrationStatus(null);
};


bcgapp.prototype.sendCalibrationParameters = function(parameters) {
  var parameterString = parameters.join(",");
  console && console.log && console.log("Sending calibration parameters: " + parameterString);
  window.SensorCalibration.sendCalibrationParameters(this.settings.sourceAddress, parameterString);
  if (this.settings.showAdaptiveCalibrationParameters) {
    $('#sentCalibrationParameters').css('display', 'block').find(".value").text(parameterString);
  }
};

bcgapp.prototype.endConfig = function() {
  $('#adaptiveCalibrationConfigOverlay').css('display', 'none');
};

bcgapp.prototype.changeTab = function(tab) {
    this.settings.tab = tab || this.settings.tab;
    $('#menu .menuButton')
        .removeClass('selected')
        .filter('#tab_' + this.settings.tab)
        .addClass('selected');
    $('#menu').removeClass().addClass(this.settings.tab);

    $('#app .contentContainer')
        .hide()
        .filter('#content_' + this.settings.tab)
        .show();

    this.saveSettingsDebounced();
};

bcgapp.prototype.saveSettings = function() {
    _.chain(this.settings)
        .keys()
        .each(function(key) {
            localStorage[key] = this.settings[key];
          // console && console.log && console.log("Saved setting " + key + " == " + this.settings[key] + " to local storage");
        }.bind(this))
        .value();
};

bcgapp.prototype.drawSettings = function() {

  $('#calibrationParameters')
    .val(this.calibration.oldParameters ? this.calibration.oldParameters[this.calibration.oldParameters.length - 1] : '7000,200,2600,2600,1000,7');
  $('#sensorIp')
    .val(this.settings.sourceAddress);
  $('#exitAdaptiveCalibration, #showAdaptiveCalibrationParameters').each(function(index, elem) {
    $(elem).prop("checked", this.settings[elem.id]);
  }.bind(this));
  $('#requiredOccupiedCalibration, #requiredEmptyCalibration, #calibrationDiscardLength, #calibrationEmptyMax, #calibrationStdevMax, #calibrationGoodLimit, #calibrationOkLimit, ' +
    '#oobMovementLpAlpha, #oobAfterMovementFromMid, #oobWaitTimeAfterMovement, #oobProbabilityLimitChangeToEmpty, #oobProbabilityLimitChangeToOccupied, ' +
    '#calibMovementLpAlpha, #calibAfterMovementFromMid, #calibWaitTimeAfterMovement, #calibProbabilityLimitChangeToEmpty, #calibProbabilityLimitChangeToOccupied,' +
    '#calibrationExp, #oobProbabilityLpAlpha, #oobQualityLpAlpha, #calibProbabilityLpAlpha, #calibQualityLpAlpha')
    .each(function(index, elem) {
      $(elem).val(this.settings[elem.id]);
    }.bind(this));
  // online/offline
  $('#settingsOnline, #settingsOffline')
    .removeClass('dimmed')
    .filter(this.settings.mode === 'offline' ? '#settingsOnline' : '#settingsOffline')
    .addClass('dimmed');
  $('#onlineOfflineExplanation').text(
    this.settings.mode === 'online' ?
      'Data will be read from the sensor.' :
      'Random sample data is generated.'
  );

  $('.settingsOnline').css('opacity', (this.settings.mode === 'offline') ? 0.1 : 1.0);

};

bcgapp.prototype.updateDials = function() {
  // updade signal strength in settings page even if we don't have many readings
  this.updateSettingsView();
  this.updateMainView();
};

bcgapp.prototype.updateSettingsView = function() {
  var signalStrength = 'N/A';
  if (
    this.settings.mode === 'online' &&
      this.fetchedHistory &&
      this.fetchedHistory.length
  ) {
    signalStrength = this.fetchedHistory[this.fetchedHistory.length - 1][5];
  }
  $('#signalStrength').text(signalStrength);
  if (this.calibration.status) {
    this.updateCalibrationView();
  }
};

bcgapp.prototype.updateMainView = function() {

  // we need at least 10 readings to show the dials
  if (this.fetchedHistory && this.fetchedHistory.length >= 10) {
    this.inbedAnalyze(this.fetchedHistory, this.inbed);
    var inbed = this.getInbed(this.inbed);
    this.graphs.setValues(window.convertMeasurementsToDisplayableFormat(this.fetchedHistory), inbed);
  } else {
    var latestStatus = 0;
    if (this.fetchedHistory && this.fetchedHistory.length > 0) {
      latestStatus = this.fetchedHistory[0][6];
    }
    this.graphs.setValues({
      hr: 27.5,
      rr: 0,
      sv: 0,
      hrv: 0,
      ss: 0,
      status: latestStatus
    }, 0);
  }
};

bcgapp.prototype.getValues = function(data) {
  return {
    hr: data[0][1],
    rr: data[0][2],
    hrv: data[0][4],
    ss: data[0][5],
    status: data[0][6]
  };

};
bcgapp.prototype.inbedInit = function() {
  console && console.log && console.log("Allocating and initializing inbed.");
  var inbed = {
    config: Module.ccall('allocate_config', 'number'),
    data: Module.ccall('allocate_data', 'number')
  };
  Module.ccall('out_of_bed_init', null, ['number', 'number'],
               [inbed.config, inbed.data]);
  return inbed;
};

bcgapp.prototype.inbedFree = function(inbed) {
  console && console.log && console.log("Freeing inbed.");
  Module._free(inbed.config);
  Module._free(inbed.data);
};

bcgapp.prototype.inbedConfig = function(config, probabilityLpAlpha, qualityLpAlpha, movementLpAlpha, afterMovementFromMid,
                                        waitTimeAfterMovement, probabilityLimitChangeToEmpty, probabilityLimitChangeToOccupied) {
  Module.ccall('set_config_values', null,
               ['number', 'number', 'number', 'number', 'number', 'number', 'number', 'number'],
               [config, probabilityLpAlpha, qualityLpAlpha, movementLpAlpha,
                afterMovementFromMid, waitTimeAfterMovement, probabilityLimitChangeToEmpty, probabilityLimitChangeToOccupied]);
};

bcgapp.prototype.inbedAnalyze = function(data, inbed) {
  var dataRow = data[data.length - 1];
  var parr = Module._malloc(8 * dataRow.length);
  var arr = new Float64Array(Module.HEAPF64.buffer, parr, dataRow.length);
  arr.set(dataRow);
  Module.ccall('out_of_bed_analyze', null, ['number', 'number', 'number'],
               [parr, inbed.data, inbed.config]);
  Module._free(parr);
};

bcgapp.prototype.getInbed = function(inbed) {
  var inbedArr = Module.ccall('get_inbed', 'number', ['number'], [inbed.data]);
  var inbedProbability = Module.getValue(inbedArr, 'double');
  var inbed = Module.getValue(inbedArr + 8, 'double');
  return {inbedProbability: inbedProbability, inbed: inbed};
};

bcgapp.prototype.autoconnect = function() {
    window.SensorReader.stopPolling();
    this.polling = false;
    setTimeout(function startPollingAfterTwoSeconds() {
        this.settings.mode = "online";
    }.bind(this), 2000);
};

bcgapp.prototype.parseCsv = function(data) {

    return _.chain(data.split('\n'))
        .reject(function filterEmptyLines(item) {
            return item.trim().length === 0;
        })
        .map(function splitByComma(item) {
            return item.split(',');
        })
        .sortBy(function oldestFirst(item) {
            return item[0];
        })
        .map(function converToNumbers(item) {
            return _.map(item, function(part) {
                return parseInt(part);
            });
        })
        .value();
};

bcgapp.prototype.fetchData = function() {

    if (this.settings.mode === "offline") {

        if (this.polling) {
            window.SensorReader.stopPolling();
            this.polling = false;
        }

      var randomData = this.generateRandomData(20 * 1000, true);
      this.fetchedHistory = randomData;

    } else {

        if (!this.polling) {
            window.SensorReader.startPolling(this.settings.sourceAddress);
            //window.SensorReader.startPolling("192.168.1.185");
            this.polling = true;
        }

        var communicationError = false;

        var recentReadings = window.SensorStorage.getHistory(10);

        // we have data!
        if (recentReadings && recentReadings.indexOf('\n') !== -1) {
            this.fetchedHistory = this.parseCsv(recentReadings);
        } else {
            this.emptyReceivedDataCount++;
            if (this.emptyReceivedDataCount > 3) {
                communicationError = true;
            }
        }

        // check that latest item received is not very very old
        if (typeof recentReadings === "string" && recentReadings.trim().length > 6) {
            var latestTimestamp = recentReadings.split('\n')[0].split(',')[0];
            var now = new Date().getTime();

            // is latest reading over 5 seconds old?
            if (now - latestTimestamp > 5000) {
                communicationError = true;
            } else {
                communicationError = false;
                this.emptyReceivedDataCount = 0;
            }
        }

        // don't raise comm. error for the first 5 seconds
        if (new Date().getTime() - this.appStartTime < 5000) {
            communicationError = false;
        }

        // if error and error is not shown, show it!
        if (communicationError && $('#connectionAlert:visible').length === 0) {
            $('#connectionAlert').fadeIn();
            $('#signalStrength').text("No connection!");
        }

        // if everything is fine, hide the error
        if (!communicationError) {
            $('#connectionAlert:visible').fadeOut();
            this.emptyReceivedDataCount = 0;
        }

    }
  if (this.calibration.status) {
    this.updateCalibrationData();
  }
  this.updateDials();
};

bcgapp.prototype.sendCalibrationParametersCallback = function(data) {
  console && console.log && console.log("Calibration parameters sent. " + data);
};

bcgapp.prototype.generateRandomData = function(millisecondsBack, generateSignalStrength) {

    var randomData = [];

    var now = new Date().getTime();
    var fakeTime;
    for (var i = 0; i < Math.ceil(millisecondsBack / 1000); i++) {
        fakeTime = now - i * 1000;
        randomData.push([
          now - i * (1000),
          61 + Math.round(Math.abs(Math.sin(fakeTime / 70000) * 30)), // HR
          Math.round(11 + Math.sin(fakeTime / 12000) * 2), // RR
          Math.round(58 + Math.sin(fakeTime / 2000) * 2), // SV
          Math.round(105 + Math.sin(fakeTime / 4800) * 4), // HRV
          generateSignalStrength ? Math.round(Math.random() *
                                              Math.sin(fakeTime / 60000) < 0.9 ? 1000 : 10000) : 'N/A', // signal
            Math.round(Math.sin(fakeTime / 60000) < 0.9 ? 1 : (Math.sin(fakeTime / 60000) < 0.95 ? 2 : 0)), // status
          Math.round(542 - Math.sin(fakeTime) * 40), // b2b
          Math.round(603 - Math.sin(fakeTime / 7) * 30), // b2b-1
          0 // b2b-2
        ]);
    }

    return randomData;
};

document.addEventListener('deviceready', function() {
    $(function() {
        window.app = new bcgapp();
        window.app.changeTab(null);
        window.app.drawSettings();
        setInterval(window.app.fetchData.bind(window.app), 1000);

        function resize() {

            var w = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
            var h = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);

            if (w === this.previousViewport.width && h < this.previousViewport.height) {
                // if viewport only gets less tall, this is a "pick an item from pulldown"-event
                // so don't accidentally change the viewing mode from portrait to landscape
            } else {
                if (h > w) {
                    $('body').removeClass('landscape').addClass('portrait');
                    $('#menu').hide();
                    setTimeout(function() {
                        $('#menu').show();
                    });
                } else {
                    setTimeout(function() {
                        $('body').removeClass('portrait').addClass('landscape');
                    }, 100);
                }
            }

            this.previousViewport = {
                height: h,
                width: w
            };

        }

        $(window).resize(_.debounce(resize.bind(window.app), 200));
        resize.call(window.app);


    }.bind(this));
}, false);
