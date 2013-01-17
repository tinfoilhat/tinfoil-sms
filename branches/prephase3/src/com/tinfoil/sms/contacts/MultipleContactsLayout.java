/** 
 * Copyright (C) 2011 Tinfoilhat
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tinfoil.sms.contacts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;

public class MultipleContactsLayout extends RelativeLayout implements Checkable {

    CheckedTextView checkbox;

    public MultipleContactsLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public MultipleContactsLayout(final Context context) {
        super(context);
    }

    public boolean isChecked() {
        if (this.checkbox != null)
        {
            return this.checkbox.isChecked();
        }
        return false;
    }

    public void setChecked(final boolean checked) {
        if (this.checkbox != null)
        {
            this.checkbox.setChecked(checked);
        }

    }

    public void toggle() {
        if (this.checkbox != null)
        {
            this.checkbox.toggle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // find checked text view
        final int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            final View v = this.getChildAt(i);
            if (v instanceof CheckedTextView) {
                this.checkbox = (CheckedTextView) v;
            }
        }
    }

}
