/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.click.jquery.examples.page.taconite.example;

import net.sf.click.jquery.JQEvent;
import net.sf.click.jquery.behavior.JQBehavior;
import net.sf.click.jquery.examples.page.BorderPage;
import net.sf.click.jquery.taconite.JQCommand;
import net.sf.click.jquery.taconite.JQTaconite;
import org.apache.click.Control;
import org.apache.click.Partial;
import org.apache.click.control.ActionLink;
import org.apache.commons.lang.math.NumberUtils;

/**
 *
 */
public class Counter extends BorderPage {

	private static final long serialVersionUID = 1L;

    public ActionLink link = new ActionLink("link", "Counter: 0");

    @Override
    public void onInit() {
        link.addBehavior(new JQBehavior(JQEvent.CLICK) {

            @Override
            protected void addHeadElements(Control source) {
                super.addHeadElements(source);
            }

            @Override
            public Partial onAction(Control source, JQEvent eventType) {
                JQTaconite partial = new JQTaconite();
                int count = NumberUtils.toInt(link.getParameter("count"));
                ++count;
                link.setParameter("count", Integer.toString(count));
                link.setLabel("Counter: " + Integer.toString(count));
                JQCommand command = new JQCommand("replace", link).characterData(true);

                // link normally contian '&' which breaks XML parsing. TODO update
                // Click Link's to use &amp; instead

                partial.add(command);
                return partial;
            }
        });
    }
}