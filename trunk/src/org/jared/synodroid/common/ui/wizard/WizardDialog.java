/**
 * 
 */
package org.jared.synodroid.common.ui.wizard;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.jared.synodroid.ds.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * This is a Wizard diaog
 * 
 * @author Eric Taix
 */
public class WizardDialog extends Dialog implements WizardInterface {

  private ArrayList<WizardStep> steps = new ArrayList<WizardStep>();
  private Button nextBtn;
  private Button prevBtn;
  private String title;
  // The current step index (starting from 0)
  private int currentStep = 0;
  // The content
  private ViewFlipper content;
  private String stepTemplate;
  private TextView stepText;

  /**
   * Constructor which initialiaz the context
   * 
   * @param contextP
   */
  public WizardDialog(Context contextP, String titleP) {
    super(contextP);
    title = titleP;
    stepTemplate = contextP.getText(R.string.wizard_step).toString();
  }

  /*
   * (non-Javadoc)
   * @see android.app.Dialog#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceStateP) {
    super.onCreate(savedInstanceStateP);
    requestWindowFeature(Window.FEATURE_LEFT_ICON);
    setContentView(R.layout.wizard);
    setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);
    setTitle(title);
    content = (ViewFlipper) findViewById(R.id.view_flipper_id);
    prevBtn = (Button) findViewById(R.id.prev_btn_id);
    prevBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View vP) {
        previous();
      }
    });
    prevBtn.setEnabled(false);
    nextBtn = (Button) findViewById(R.id.next_btn_id);
    nextBtn.setOnClickListener(new View.OnClickListener() {
      public void onClick(View vP) {
        next();
      }
    });
    stepText = (TextView)findViewById(R.id.step_text_id);
    updateSteps();
    updateUI();
  }

  /**
   * Add a new step
   * 
   * @param stepP
   */
  public void addStep(WizardStep stepP) {
    steps.add(stepP);
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardInterface#next()
   */
  public void next() {
    currentStep++;
    content.showNext();
    updateUI();
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardInterface#previous()
   */
  public void previous() {
    currentStep--;
    content.showPrevious();
    updateUI();
  }

  /**
   * Update buttons according to the current step and state
   */
  private void updateUI() {
    // Enable / Disable the next step
    boolean bool = false;
    if (currentStep < steps.size() - 1) {
      bool = true;
      nextBtn.setText(getContext().getText(R.string.wizard_button_next));
    }
    else if (currentStep == steps.size() - 1) {
      nextBtn.setText(getContext().getText(R.string.wizard_button_finish));
      bool = true;
    }
    nextBtn.setEnabled(bool);
    // Enable / Disable the previous step
    bool = false;
    if (currentStep > 0) {
      bool = true;
    }
    // Update next button text to "finish"
    prevBtn.setEnabled(bool);
    // Update step text
    String msg = MessageFormat.format(stepTemplate, new Object[] { currentStep+1, steps.size() });
    stepText.setText(msg.toString());    
  }

  /**
   * Update all steps and set the current step to index 0
   */
  private void updateSteps() {
    content.removeAllViews();
    // Add all steps
    for (WizardStep step : steps) {
      step.init(this);
      View view = step.getView();
      content.addView(view);
    }
    currentStep = 0;
  }

  /* (non-Javadoc)
   * @see org.jared.synodroid.common.ui.wizard.WizardInterface#reset()
   */
  public void reset() {
    currentStep = 0;
    for (WizardStep step : steps) {
      step.reset();
    }
    View view = content.getChildAt(0);
    view.bringToFront();
  }



}
