window.percentile = function(data, percent) {
  if (data) {
    var dataSorted = window.mat.sort(data);
    var percentIndex = Math.floor(window.mat.length(dataSorted) * percent);
    if (percentIndex === 0) {
      return dataSorted[0];
    } else {
      return dataSorted[percentIndex - 1];
    }
  } else {
    console && console.log && console.log('Percentile: no data');
    return 0;
  }
};
