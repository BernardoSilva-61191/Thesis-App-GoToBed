% THIS SOFTWARE IS PROVIDED BY MURATA "AS IS" AND
% ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
% LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
% FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
% MURATA BE LIABLE FOR ANY DIRECT, INDIRECT,
% INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
% (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
% SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
% HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
% STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
% IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
% POSSIBILITY OF SUCH DAMAGE.

function [ parameters, calcSS, calcSV, emptyLevel ] = adaptive(occupiedSS,occupiedSV,emptySS, parameters)
%ADAPTIVE Optimizes BCG parameters based on occupied Signal Strength,
%stroke volume and empty bed level
%   Input parameters: array of occupied signal strengths, array of occupied
%   stroke volumes, empty bed signal strength level.
%   Output: array of BCG calibration parameters
%   [var_level_1,var_level_2, stroke_volume, tentative_stroke_volume,
%   signal_range,to_micro_g]. tentative_stroke_volume = 0, to_micro_g = 7

%% Check which adaptive calibration method to use
% Ratio of non-zero stroke volume readings
svNonZeroRatio = nnz(occupiedSV) / length(occupiedSV);
% Stroke volume variation
svDiff = sum(abs(diff(nonzeros(occupiedSV)))) / length(nonzeros(occupiedSV));
var1_min = 4000;
svDiff_min = 0.25;
svNonZeroRatio_min = 0.5;
empty_scaler = 0.25;
occupiedPercentile = 0.25;  % 0.5 = median
emptyPercentile = 0.5;

occupiedSS = nonzeros(occupiedSS);
occupiedSV = nonzeros(occupiedSV);
calcSS = percentile(occupiedSS, occupiedPercentile);
calcSV = percentile(occupiedSV, occupiedPercentile);
if svDiff > svDiff_min && svNonZeroRatio > svNonZeroRatio_min
    %% Adaptive calibration based on stroke_volume and signal strength 
%     fprintf(1,'Running adaptive calibration based on stroke volumes and signal strengths.\n');
    var1_multiplier = 7;            % from signal strength
    signal_range_multiplier = 45;   % from stroke volume
    stroke_vol_multiplier = 2.6;    % from signal range

    % calculate parameters
    var_level_1 = max(var1_min, var1_multiplier * calcSS);
    if var_level_1 > 15000  % limits var_level_1 of going too high
        var_level_1 = 15000 + (var_level_1-15000) / 2;
    end
    signal_range = calcSV * signal_range_multiplier;
    stroke_vol = round(stroke_vol_multiplier * signal_range);
else
    %% Adaptive calibration based on only signal strength
%     fprintf(1,'Running adaptive calibration based only on signal strengths.\n');
    var1_multiplier = 7;            % from signal strength
    signal_range_multiplier = 2.7;  % from signal strength
    stroke_vol_multiplier = 2.6;    % from signal range
    % calculate parameters
    calcSS = percentile(occupiedSS, occupiedPercentile);
    var_level_1 = max(var1_min, var1_multiplier * calcSS);
    if var_level_1 > 15000  % limits var_level_1 of going too high
        var_level_1 = 15000 + (var_level_1-15000) / 2;
    end
    signal_range = calcSS * signal_range_multiplier;
    stroke_vol = round(stroke_vol_multiplier * signal_range);
end
emptyLevel = percentile(emptySS, emptyPercentile); 
var_level_2 = emptyLevel + empty_scaler*(calcSS-emptyLevel);    % var_level_2 is between emptyLevel and percentile of occupied Signal Strengths
parameters(:) = round([var_level_1, var_level_2, stroke_vol, stroke_vol, signal_range, 7]);
