
window.adaptive = function(occupiedSS, occupiedSV, emptySS) {
  var svNonZeroRatio = window.mat.nnz(occupiedSV) / window.mat.length(occupiedSV);
  var svDiff = window.mat.sum(window.mat.abs(window.mat.diff(window.mat.nonzeros(occupiedSV)))) /
      window.mat.length(window.mat.nonzeros(occupiedSV));

  var var1_min = 4000;
  var svDiff_min = 0.25;
  var svNonZeroRatio_min = 0.5;
  var empty_scaler = 0.25;
  var occupiedPercentile = 0.25;
  var emptyPercentile = 0.5;

  var nonZeroOccupiedSS = window.mat.nonzeros(occupiedSS);
  var nonZeroOccupiedSV = window.mat.nonzeros(occupiedSV);
  var calcSS = window.percentile(nonZeroOccupiedSS, occupiedPercentile);
  var calcSV = window.percentile(nonZeroOccupiedSV, occupiedPercentile);
  var emptyLevel = window.percentile(emptySS, emptyPercentile);

  var var1_multiplier;
  var signal_range_multiplier;
  var stroke_vol_multiplier;
  var var_level_1;
  var signal_range;
  var stroke_vol;
  if (svDiff > svDiff_min && svNonZeroRatio > svNonZeroRatio_min) {
    var1_multiplier = 7;
    signal_range_multiplier = 45;
    stroke_vol_multiplier = 2.6;

    var_level_1 = window.mat.max(var1_min, var1_multiplier * calcSS);
    if (var_level_1 > 15000) {
      var_level_1 = 15000 + (var_level_1 - 15000) / 2;
    }
    signal_range = calcSV * signal_range_multiplier;
    stroke_vol = window.mat.round(stroke_vol_multiplier * signal_range);
  } else {
    var1_multiplier = 7;
    signal_range_multiplier = 2.7;
    stroke_vol_multiplier = 2.6;
    calcSS = window.percentile(occupiedSS, occupiedPercentile);
    var_level_1 = window.mat.max(var1_min, var1_multiplier * calcSS);
    if (var_level_1 > 15000) {
      var_level_1 = 15000 + (var_level_1-15000) / 2;
    }
    signal_range = calcSS * signal_range_multiplier;
    stroke_vol = window.mat.round(stroke_vol_multiplier * signal_range);
  }

  var var_level_2 = emptyLevel + empty_scaler * (calcSS - emptyLevel);
  parameters = window.mat.round([var_level_1, var_level_2, stroke_vol,
                                     stroke_vol, signal_range, 7]);

  return {
    parameters: parameters,
    calcSS: calcSS,
    calcSV: calcSV,
    emptyLevel: emptyLevel
  };
};
