/*
 * 게임 오버라이드(원본은 CFR 디컴파일). 추가 악세사리(mods/accs)를 모든 스타일의 사용 가능 목록에 넣는다.
 */
package com.cyberstep.ring.data;

import com.cyberstep.ring.data.StyleData;
import java.io.Serializable;
import java.util.Vector;

public class NewSetting
implements Serializable {
    public static final int serialVersionUID = 1;
    static NewSetting setting = new NewSetting();
    Vector styles = new Vector();
    /** mods/accs 로 만든 악세사리. 빌드 시 .accs 가 resource/accs/ 에 생성된다. */
    static final String[] EXTRA_ACCESSORIES = {
        "/accs/heavygloves.accs",
        "/accs/volcano.accs",
    };

    public static void setSetting(NewSetting newSetting) {
        setting = newSetting;
    }

    static void putStyleData(StyleData styleData) {
        NewSetting.setting.styles.add(styleData);
    }

    public static StyleData getStyleData(String string) {
        int n = 0;
        while (n < NewSetting.setting.styles.size()) {
            StyleData styleData = (StyleData)NewSetting.setting.styles.get(n);
            if (styleData.name.equals(string)) {
                return styleData;
            }
            ++n;
        }
        return null;
    }

    static {
        StyleData styleData = new StyleData("KAKUTOU_M", "/player/style/kakutoum", "ym", 5, 4, 5, 7, 6, 1.0f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("KAKUTOU_F", "/player/style/kakutouf", "yw", 4, 5, 6, 7, 5, 0.95f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("ARMY_W", "/player/style/armyw", "wm", 6, 7, 4, 4, 5, 1.05f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("ARMY_B", "/player/style/armyb", "bm", 6, 7, 4, 4, 5, 1.05f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("SPYW", "/player/style/spyw", "ww", 3, 8, 7, 7, 4, 0.95f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("SPYB", "/player/style/spyb", "bw", 3, 8, 7, 7, 4, 0.95f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("SUPERMAN_Y", "/player/style/supermany", "ym", 8, 2, 4, 3, 5, 1.1f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("SUPERMAN_B", "/player/style/supermanb", "bm", 8, 2, 3, 3, 6, 1.1f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("ARMOR_Y", "/player/style/armory", "ym", 3, 4, 3, 2, 8, 0.9f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("ARMOR_W", "/player/style/armorw", "wm", 3, 4, 3, 2, 8, 0.9f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("ESPER_Y", "/player/style/espery", "yw", 3, 5, 5, 5, 4, 0.9f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        styleData = new StyleData("ESPER_W", "/player/style/esperw", "ww", 4, 5, 4, 5, 4, 0.98f);
        styleData.useableaccessories.add("/accs/rucksack1.accs");
        styleData.useableaccessories.add("/accs/rucksack2.accs");
        styleData.useableaccessories.add("/accs/bracelet1.accs");
        styleData.useableaccessories.add("/accs/porch1.accs");
        styleData.useableaccessories.add("/accs/porch2.accs");
        styleData.useableaccessories.add("/accs/herobelt1.accs");
        styleData.useableaccessories.add("/accs/herobelt2.accs");
        styleData.useableaccessories.add("/accs/backshield1.accs");
        styleData.useableaccessories.add("/accs/backshield2.accs");
        styleData.useableaccessories.add("/accs/boxinggrab1.accs");
        styleData.useableaccessories.add("/accs/boxinggrab2.accs");
        NewSetting.putStyleData(styleData);
        // --- 추가 악세사리(mods/accs/*.txt → resource/accs/*.accs) ---
        for (int i = 0; i < NewSetting.setting.styles.size(); ++i) {
            StyleData sd = (StyleData)NewSetting.setting.styles.get(i);
            for (int j = 0; j < EXTRA_ACCESSORIES.length; ++j) {
                sd.useableaccessories.add(EXTRA_ACCESSORIES[j]);
            }
        }
    }
}

