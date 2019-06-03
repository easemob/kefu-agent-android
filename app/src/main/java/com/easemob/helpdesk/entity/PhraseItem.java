package com.easemob.helpdesk.entity;

import com.hyphenate.kefusdk.entity.HDPhrase;

/**
 * Created by benson on 2018/4/27.
 */

public class PhraseItem {

    private HDPhrase phrase;

    public HDPhrase getPhrase() {
        return phrase;
    }

    public void setPhrase(HDPhrase phrase) {
        this.phrase = phrase;
    }

    /**
     * 是否展开
     */
    private boolean isExpand;

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    /**
     * 级联级别
     */
    private int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private long belongAncestor;

    public long getBelongAncestor() {
        return belongAncestor;
    }

    public void setBelongAncestor(long belongAncestor) {
        this.belongAncestor = belongAncestor;
    }

    @Override public boolean equals(Object obj) {

        if (obj instanceof PhraseItem) {
            PhraseItem item = (PhraseItem) obj;
            return phrase.id == item.getPhrase().id;
        }
        return false;
    }
}
