"use strict";

// this code has been converted from matlab to js
var convertMeasurementsToDisplayableFormat = function(data, returnMultiple) {

    if (!data || !data.length) {
        return null;
    }

    var in_mat = data;

    in_mat.flattened = _.chain(in_mat).flatten().value();
    in_mat.flattenedLength = in_mat.flattened.length;
    in_mat.originalItemLength = in_mat[0].length;
    in_mat.pick = function(column, multiplier) {
        var result = [];
        var i, j;
        for (i = column - 1; i < this.flattenedLength; i += this.originalItemLength) {
            result.push(this.flattened[i] * (multiplier ? multiplier : 1));
        }

        var completeResult = [result];
        for (i = 0; i < in_mat.length; i++) {
            var emptyRow = new Array(this.originalItemLength);
            for (j = 0; j < this.originalItemLength; j++) emptyRow[j] = 0;
            completeResult.push(emptyRow);
        }

        completeResult.set = function(col, row, value) {
            this[row - 1][col - 1] = value;
        };

        completeResult.get = function(col, row) {
            return this[row - 1][col - 1];
        };

        return completeResult;
    };

    var len = in_mat.length;

    var timestamps = in_mat.pick(1);

    var HR = in_mat.pick(2);

    var HRV = in_mat.pick(5);

    var SV = in_mat.pick(4);

    SV.set(1, 2, SV.get(1, 1));

    SV.set(1, 3, 0);

    var klp = 0.25;

    var klp2 = 0.0625;

    var SVmax = 150;

    var HRVmax = 150;

    var status_ = in_mat.pick(7);
    status_.set(1, 2, status_.get(1, 1));

    var RR = in_mat.pick(3);

    var toZeroCount = 0;

    var hr_lp = HR[0][0];

    var hrv_lp = HRV[0][0];

    var sv_lp = [SV.get(1, 2), SV.get(1, 3)];

    var pulseCount = 0;

    for (var i = 2; i < len + 1; i++) {

        if (i == 1) {
            status_.set(i, 2, status_.get(i, 1));
        } else if (i == 2) {
            status_.set(i, 2, status_.get(i - 1, 2));
        } 
        else {
            if (status_.get(i, 1) == status_.get(i - 1, 1)) {
                status_.set(i, 2, status_.get(i, 1));
            } else {
                status_.set(i, 2, status_.get(i - 2, 1));
            }
        }


        if ((HR.get(i, 1) > 35) && ((HR.get(i, 1) != HR.get(i - 1, 1)) || (SV.get(i, 1) != SV.get(i - 1, 1)) || (HRV.get(i, 1) != HRV.get(i - 1, 1))) && (status_.get(i, 2) == 1)) {

            toZeroCount = 0;
            pulseCount++;

            if (1 / pulseCount > klp) {
                hr_lp = hr_lp * (1 - 1 / pulseCount) + (1 / pulseCount) * HR.get(i, 1);
            } else {
                hr_lp = hr_lp * (1 - klp) + klp * HR.get(i, 1);
            }

            HR.set(i, 1, hr_lp);

            if ((HRV.get(i, 1) > 0) && (HRV.get(i, 1) < HRVmax)) {
                if (1 / pulseCount > klp) {
                    hrv_lp = hrv_lp * (1 - 1 / pulseCount) + (1 / pulseCount) * HRV.get(i, 1);
                } else {
                    hrv_lp = hrv_lp * (1 - klp) + klp * HRV.get(i, 1);
                }
                HRV.set(i, 1, hrv_lp);
            } else {
                HRV.set(i, 1, HRV.get(i - 1, 1));
            }

            if ((SV.get(i, 1) > 0) && (SV.get(i, 1) < SVmax)) {

                sv_lp[0] = sv_lp[0] * (1 - klp2) + klp2 * SV.get(i, 1);
                SV.set(i, 2, sv_lp[0]);
                SV.set(i, 3, (SV.get(i, 1) - SV.get(i, 2)) / SV.get(i, 2));

                sv_lp[1] = sv_lp[1] * (1 - klp) + klp * SV.get(i, 3);

                SV.set(i, 3, sv_lp[1] + 1);

                if (SV.get(i, 3) > 1.125) {
                    SV.set(i, 3, 1.125);
                } else if (SV.get(i, 3) < 0.9) {
                    SV.set(i, 3, 0.9);
                }

            } else {
                SV.set(i, 1, SV.get(i - 1, 1));
                SV.set(i, 2, SV.get(i - 1, 2));
                SV.set(i, 3, SV.get(i - 1, 3));
            }

        } else {

            if (status_.get(i, 2) == 2 || status_.get(i, 2) == 1) {
                toZeroCount = toZeroCount + 1;
            } else {
                toZeroCount = toZeroCount + 2;
            }

            if (toZeroCount > 8) {

                HR.set(i, 1, 0);
                HRV.set(i, 1, 0);
                SV.set(i, 1, 0);
                SV.set(i, 2, 0);
                SV.set(i, 3, 0);
                RR.set(i, 1, 0);

            } else {

                HR.set(i, 1, HR.get(i - 1, 1));
                HRV.set(i, 1, HRV.get(i - 1, 1));
                SV.set(i, 1, SV.get(i - 1, 1));
                SV.set(i, 2, SV.get(i - 1, 2));
                SV.set(i, 3, SV.get(i - 1, 3));
                RR.set(i, 1, RR.get(i - 1, 1));
            }

        }

    }

    function toResultObject(position) {
        return {
            time: timestamps[0][position],
            status: status_[1][position],
            hr: HR[0][position],
            hrv: HRV[0][position],
            sv: SV[0][position],
            rr: RR[0][position]
        };
    }

    // return only latest result
    if (!returnMultiple) {
        return toResultObject(len - 1);
    } else {
        return _.map(_.range(len - 1), function convert(pos) {
            return toResultObject(pos);
        });
    }

};
