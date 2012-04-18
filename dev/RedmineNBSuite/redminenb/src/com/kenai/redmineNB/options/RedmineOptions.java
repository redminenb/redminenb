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
package com.kenai.redmineNB.options;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle.Messages;

/**
 * BugtrackingOptionsDialog controller
 * 
 * @author Anchialas <anchialas@gmail.com>
 */
@Messages({
    "RedmineOptions.displayName=Redmine",
    "RedmineOptions.tooltip=Redmine Options",
    "MSG_INVALID_VALUE=Invalid value.",
    "MSG_MUST_BE_GREATER_THEN_5=Must be a number greater then 5."
})
public class RedmineOptions extends AdvancedOption {

    @Override
    public String getDisplayName() {
        return Bundle.RedmineOptions_displayName();
    }

    @Override
    public String getTooltip() {
        return Bundle.RedmineOptions_tooltip();
    }

    @Override
    public OptionsPanelController create() {
        return new RedmineOptionsController();
    }

}
