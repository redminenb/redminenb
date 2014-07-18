/*
 * Copyright 2012 Anchialas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenai.redminenb.options;

import com.kenai.redminenb.RedmineConfig;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * RedmineOptionsController
 *
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
    "MSG_INVALID_VALUE=Invalid value.",
    "MSG_MUST_BE_GREATER_THEN_5=Must be a number greater then 5."
})
public final class RedmineOptionsController extends OptionsPanelController implements DocumentListener {

    private final RedmineOptionsPanel panel;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private boolean valid = false;

    public RedmineOptionsController() {
        panel = new RedmineOptionsPanel();
        panel.errorLabel.setText(null);
    }

    @Override
    public void update() {
        panel.issuesTextField.getDocument().removeDocumentListener(this); // #163955 - do not fire change events on load
        panel.queriesTextField.getDocument().removeDocumentListener(this);
        panel.issuesTextField.setText(RedmineConfig.getInstance().getIssueRefreshInterval() + "");  // NOI18N
        panel.queriesTextField.setText(RedmineConfig.getInstance().getQueryRefreshInterval() + ""); // NOI18N
        panel.issuesTextField.getDocument().addDocumentListener(this);
        panel.queriesTextField.getDocument().addDocumentListener(this);
    }

    @Override
    public void applyChanges() {
        String queryRefresh = panel.queriesTextField.getText().trim();
        int r = queryRefresh.equals("") ? 0 : Integer.parseInt(queryRefresh);   // NOI18N
        RedmineConfig.getInstance().setQueryRefreshInterval(r);

        String issueRefresh = panel.issuesTextField.getText().trim();
        r = issueRefresh.equals("") ? 0 : Integer.parseInt(issueRefresh);       // NOI18N
        RedmineConfig.getInstance().setIssueRefreshInterval(r);
    }

    @Override
    public void cancel() {
        update();
    }

    @Override
    public boolean isValid() {
        validate(false);
        return valid;
    }

    private boolean isValidRefreshValue(String s) {
        if (!s.equals("")) {                                                     // NOI18N
            try {
                int i = Integer.parseInt(s);
                if (i < 5) {
                    panel.errorLabel.setText(NbBundle.getMessage(RedmineOptionsController.class, "MSG_MUST_BE_GREATER_THEN_5"));
                    return false;
                }
            } catch (NumberFormatException e) {
                panel.errorLabel.setText(NbBundle.getMessage(RedmineOptionsController.class, "MSG_INVALID_VALUE"));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isChanged() {
        return !panel.issuesTextField.getText().trim().equals(RedmineConfig.getInstance().getIssueRefreshInterval() + "") || // NOI18N
                !panel.queriesTextField.getText().trim().equals(RedmineConfig.getInstance().getQueryRefreshInterval() + "");   // NOI18N
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public JComponent getComponent(org.openide.util.Lookup masterLookup) {
        return panel;
    }

    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        validate(true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validate(true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        validate(true);
    }

    private void validate(boolean fireEvents) {
        boolean oldValid = valid;
        panel.errorLabel.setVisible(false);
        panel.errorLabel.setText("");                                           // NOI18N

        String queryRefresh = panel.queriesTextField.getText().trim();
        String issueRefresh = panel.issuesTextField.getText().trim();

        valid = isValidRefreshValue(queryRefresh)
                && isValidRefreshValue(issueRefresh);

        panel.errorLabel.setVisible(!valid);

        if (fireEvents && oldValid != valid) {
            support.firePropertyChange(new PropertyChangeEvent(this, OptionsPanelController.PROP_VALID, oldValid, valid));
        }
    }

}
