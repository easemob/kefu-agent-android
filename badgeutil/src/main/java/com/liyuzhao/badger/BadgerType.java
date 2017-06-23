package com.liyuzhao.badger;

import android.text.TextUtils;

import com.liyuzhao.badger.impl.AdwHomeBadger;
import com.liyuzhao.badger.impl.ApexHomeBadger;
import com.liyuzhao.badger.impl.AsusHomeLauncher;
import com.liyuzhao.badger.impl.DefaultBadger;
import com.liyuzhao.badger.impl.LGHomeBadger;
import com.liyuzhao.badger.impl.NewHtcHomeBadger;
import com.liyuzhao.badger.impl.NovaHomeBadger;
import com.liyuzhao.badger.impl.SamsungHomeBadger;
import com.liyuzhao.badger.impl.SolidHomeBadger;
import com.liyuzhao.badger.impl.SonyHomeBadger;
import com.liyuzhao.badger.impl.XiaomiHomeBadger;

import java.util.List;

/**
 * Created by liyuzhao on 16/5/5.
 */
public enum BadgerType {

    DEFAULT {
        public Badger initBadger() {
            return new DefaultBadger();
        }
    }, ADW {
        public Badger initBadger() {
            return new AdwHomeBadger();
        }
    }, APEX {
        public Badger initBadger() {
            return new ApexHomeBadger();
        }
    }, ASUS {
        public Badger initBadger() {
            return new AsusHomeLauncher();
        }
    }, LG {
        public Badger initBadger() {
            return new LGHomeBadger();
        }
    }, HTC {
        public Badger initBadger() {
            return new NewHtcHomeBadger();
        }
    }, NOVA {
        public Badger initBadger() {
            return new NovaHomeBadger();
        }
    }, SAMSUNG {
        public Badger initBadger() {
            return new SamsungHomeBadger();
        }
    }, SOLID {
        public Badger initBadger() {
            return new SolidHomeBadger();
        }
    }, SONY {
        public Badger initBadger() {
            return new SonyHomeBadger();
        }
    }, XIAO_MI {
        public Badger initBadger() {
            return new XiaomiHomeBadger();
        }
    };

    public Badger badger;

    public static Badger getBadgerByLauncherName(String launcherName) {
        Badger result = new DefaultBadger();
        if (!TextUtils.isEmpty(launcherName)) {
            for (BadgerType badgerType : BadgerType.values()) {
                if (badgerType.getSupportLaunchers().contains(launcherName)) {
                    result = badgerType.getBadger();
                    break;
                }
            }
        }
        return result;
    }


    public Badger getBadger() {
        if (badger == null)
            badger = initBadger();
        return badger;
    }

    public abstract Badger initBadger();

    public List<String> getSupportLaunchers() {
        return getBadger().getSupportLaunchers();
    }

}
