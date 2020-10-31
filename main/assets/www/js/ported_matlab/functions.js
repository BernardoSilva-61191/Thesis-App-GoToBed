window.mat = {};

window.mat.nnz = function(arr) {
  return window.mat.nonzeros(arr).length;
};

window.mat.nonzeros = function(arr) {
  if (Array.isArray(arr)) {
    return arr.filter(function(elem) {
      return elem !== 0;
    });
  } else {
    return [];
  }
};

window.mat.length = function(arr) {
  if (Array.isArray(arr)) {
    return arr.length;
  } else {
    return 0;
  }
};

window.mat.std = function(values){
  var avg = window.mat.avg(values);

  var squareDiffs = values.map(function(value) {
    var diff = value - avg;
    return diff * diff;
  });

  var avgSquareDiff = window.mat.avg(squareDiffs);
  return Math.sqrt(avgSquareDiff);
};

window.mat.avg = function(data){
  return window.mat.sum(data) / data.length;
};

window.mat.sum = function(data) {
  return data.reduce(function(sum, value){
    return sum + value;
  }, 0);
};

window.mat.diff = function(arr) {
  return arr.slice(1).map(function(n, i) {
    return n - arr[i];
  });
};

window.mat.abs = function(data) {
  return data.map(function(value) {
    return Math.abs(value);
  });
};

window.mat.sort = function(data) {
  return data.slice().sort();
};

window.mat.max = function(a, b) {
  return Math.max(a, b);
};

window.mat.round = function(data) {
  if (Array.isArray(data)) {
    return data.map(function(value) {
      return window.mat.round(value);
    });
  } else if (!isNaN(data)) {
    return Math.round(data);
  } else {
    console && console.log && console.log("Round: data " + data + " is not an array or number");
    return data;
  }
};

window.mat.multiply = function(data, multiplier) {
  if (Array.isArray(data)) {
    return data.map(function(value) {
      return value * multiplier;
    });
  } else if (!isNaN(data)) {
    return data * multiplier;
  } else {
    console && console.log && console.log("Multiply: data " + data + " is not an array or number");
    return data;
  }
};

window.mat.add = function(data, addend) {
  if (Array.isArray(addend)) {
    return data.map(function(value, index) {
      return value + addend[index];
    });
  } else if (!isNaN(data)) {
    return data + addend;
  } else {
    console && console.log && console.log("Add: addend " + addend + " is not an array or number");
    return data;
  }
};

window.mat.subtract = function(data, subtrahend) {
  if (Array.isArray(subtrahend)) {
    return data.map(function(value, index) {
      return value - subtrahend[index];
    });
  } else if (!isNaN(data)) {
    return data + subtrahend;
  } else {
    console && console.log && console.log("Subtracr: subtrahend " + subtrahend + " is not an array or number");
    return data;
  }
};

window.mat.lt = function(data, comparable) {
  if (Array.isArray(data)) {
    return data.every(function(value) {
      return value < comparable;
    });
  } else if (!isNaN(data)) {
    return data < comparable;
  } else {
    console && console.log && console.log("lt: data " + data + " is not an array or number");
    return data;
  }
};
