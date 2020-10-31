function [ percentileValue ] = percentile(data, percent)
%PERCENTILE Calculates percentile from input array
%   Calculates percentile from input array at specific percentile. Input
%   parameters: array, percent (0...1). Floors to closest percentile value
%   (e.q. row 11.74 --> get value in row 11)

percentileValue = zeros(1,1);

if isempty(data)
%     warning('Percentile: no data')
    percentileValue = 0;
else
    dataSorted = sort(data);
    percentIndex = floor(length(dataSorted)*percent);
    if percentIndex == 0
        percentIndex = 1;
    end
    percentileValue = dataSorted(percentIndex);
end

