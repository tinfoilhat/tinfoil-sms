package com.tinfoil.sms.utility;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViews;
import com.espian.showcaseview.targets.ActionViewTarget;
import com.espian.showcaseview.targets.PointTarget;
import com.espian.showcaseview.targets.Target;
import com.espian.showcaseview.targets.ViewTarget;
import com.tinfoil.sms.R;
import com.tinfoil.sms.dataStructures.WalkthroughStep;
import com.tinfoil.sms.database.DBAccessor;
import com.tinfoil.sms.settings.QuickPrefsActivity;

public abstract class Walkthrough
{
    /* Enumeration of tutorial steps */
    public static enum Step 
    {
        INTRO,
        START_IMPORT,
        IMPORT,
        START_EXCHANGE,
        SET_SECRET,
        KEY_SENT,
        PENDING,
        ACCEPT,
        SUCCESS,
        CLOSE
    }
    
    /**
     * Displays the step in the walkthrough specified
     * @param step The current step to display
     */
    public static void show(Step step, Activity activity)
    {
        ShowcaseViews mViews;
        ShowcaseView sv;
        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        Target target;
        
        switch (step)
        {
        case INTRO:
            mViews = new ShowcaseViews(activity);
            
            // First step of the introductory walkthrough, initial intro
            mViews.addView( new ShowcaseViews.ItemViewProperties(R.id.empty,
                    R.string.tut_intro_title,
                    R.string.tut_intro_body,
                    0.0f));
            
            // Second step importing contacts, highlights add/import contacts 
            mViews.addView( new ShowcaseViews.ItemViewProperties(R.id.empty,
                    R.string.tut_startimport_title,
                    R.string.tut_startimport_body,
                    ShowcaseView.ITEM_ACTION_HOME,
                    1.6f));
            mViews.show();
            disableWalkthroughStep(step, activity);
            break;
            
        case START_IMPORT:
            // Done as part of the INTRO step, skip
            disableWalkthroughStep(step, activity);
            break;
        
        case IMPORT:
            // Shows importing instructions
            co.hideOnClickOutside = true;
            target = new PointTarget(200, 200);
            sv = ShowcaseView.insertShowcaseView(target, activity, R.string.tut_import_title, R.string.tut_import_boby, co);
            disableWalkthroughStep(step, activity);
            break;
            
        case START_EXCHANGE:
            // Display the start new key exchange instructions
            target = new ActionViewTarget(activity, ActionViewTarget.Type.OVERFLOW);
            sv = ShowcaseView.insertShowcaseView(target, activity, R.string.tut_startexchange_title, 
                    R.string.tut_startexchange_body, co);
            sv.setScaleMultiplier(0.5f);
            disableWalkthroughStep(step, activity);
            break;
        
        case SET_SECRET:
            // Show the tutorial for setting shared secrets
            target = new ViewTarget(R.id.new_message_number, activity);
            sv = ShowcaseView.insertShowcaseView(target, activity, R.string.tut_setsecret_title, R.string.tut_setsecret_body);
            disableWalkthroughStep(step, activity);
            break;
            
        case KEY_SENT:
            // Display the key sent step of tutorial
            target = new ViewTarget(R.id.key_exchange, activity);
            sv = ShowcaseView.insertShowcaseView(target, activity, R.string.tut_keysent_title, R.string.tut_keysent_body);
            sv.setScaleMultiplier(0.0f);
            disableWalkthroughStep(step, activity);
            break;
            
        case PENDING:
            // Show the tutorial for pending key exchanges
            target = new ViewTarget(R.id.button_layout, activity);
            sv = ShowcaseView.insertShowcaseView(target, activity, R.string.tut_pending_title, R.string.tut_pending_body);      
            sv.setScaleMultiplier(0.0f);
            disableWalkthroughStep(step, activity);
            break;
            
        case ACCEPT:
            // Requires listener handled by showWithListener
            break;
            
        case SUCCESS:
            mViews = new ShowcaseViews(activity);
            
            // Final message if a successful key exchange occurred and all other messages shown
            mViews.addView( new ShowcaseViews.ItemViewProperties(R.id.empty,
                R.string.tut_success_title,
                R.string.tut_success_body,
                0.0f));
        
            // Second step importing contacts, highlights add/import contacts 
            mViews.addView( new ShowcaseViews.ItemViewProperties(R.id.empty,
                    R.string.tut_close_title,
                    R.string.tut_close_body,
                    0.0f));
            mViews.show();
            disableWalkthroughStep(step, activity);
            break;
            
        case CLOSE:
            // Done as part of the SUCCESS step, skip
            disableWalkthroughStep(step, activity);
            break;
            
        default:
            Log.v("Walkthrough", "Invalid step given!");
            break;
        }
    }
    
    /**
     * Displays the step in the walkthrough specified as well as with a listener
     * for when the tutorial is hidden.
     * @param step The current step to display
     * @param listener A listener that is executed after tutorial is hidden.
     */
    public static void showWithListener(Step step, Activity activity, OnShowcaseEventListener listener)
    {
        ShowcaseView sv;
        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        Target target;
        
        switch (step)
        {
        case ACCEPT:
            // Show the accept tutorial when the user accepts key exchange;
            co.hideOnClickOutside = true;

            target = new ViewTarget(R.id.button_layout, activity);
            sv = ShowcaseView.insertShowcaseView(target, activity, 
                       R.string.tut_accept_title, R.string.tut_accept_body);
            
            sv.setOnShowcaseEventListener(listener);
            disableWalkthroughStep(step, activity);
            break;
            
        default:
            Log.v("Walkthrough", "Invalid tutorial step for showWithListener");
            break;
        }
    }
    
    /**
     * Returns true if the step in the walkthrough has already been shown.
     * @param step The step in the walkthrough
     * @return
     */
    public static boolean hasShown(Step step, Activity activity)
    {
        if (PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext()).getBoolean(
                QuickPrefsActivity.ENABLE_WALKTHROUGH_SETTING_KEY, true))
        {
            DBAccessor dba = new DBAccessor(activity);
            WalkthroughStep ws = dba.getWalkthrough();
            return ws.get(step);
        }
        
        // Return true regardless if walkthrough disabled
        return true;
    }
    
    /**
     * Enables a step in the walkthrough in the application settings preferences, this should
     * only be called upon completion of the walkthrough.
     */
    public static void enableWalkthroughStep(Step step, Activity activity)
    {
        DBAccessor dba = new DBAccessor(activity);
        WalkthroughStep ws = new WalkthroughStep(null);
        ws.set(step, false);
        dba.updateWalkthrough(ws);
    }
        
    /**
     * Disables a step in the walkthrough in the application settings preferences, this should
     * only be called upon completion of the walkthrough.
     */
    private static void disableWalkthroughStep(Step step, Activity activity)
    {
        DBAccessor dba = new DBAccessor(activity);
        WalkthroughStep ws = new WalkthroughStep(null);
        ws.set(step, true);
        dba.updateWalkthrough(ws);
    }
    
    /**
     * Enables the whole walkthrough by initializing every step as not having been viewed,
     * this should be executed the first time the application is started.
     */
    public static void enableWalkthrough(Activity activity)
    {
        WalkthroughStep ws = new WalkthroughStep(false);
        DBAccessor dba = new DBAccessor(activity);
        dba.updateWalkthrough(ws);
    }
    
    /**
     * Disables the whole walkthrough by initializing every step as not having been viewed,
     * this should be executed the first time the application is started.
     */
    public static void disableWalkthrough(Activity activity)
    {
        WalkthroughStep ws = new WalkthroughStep(true);
        DBAccessor dba = new DBAccessor(activity);
        dba.updateWalkthrough(ws);
    }
}
