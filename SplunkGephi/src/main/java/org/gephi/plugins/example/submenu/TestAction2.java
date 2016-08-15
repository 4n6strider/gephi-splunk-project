/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.plugins.example.submenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

/**
 * Example of an action accessible from the "Plugins" menu un the menubar.
 * <p>
 * The annotations on the class defines the menu's name, position and class.
 * 
 * @author Mathieu Bastian
 */
@ActionID(category = "File",
id = "org.gephi.desktop.filters.TestAction2")
@ActionRegistration(displayName = "#CTL_TestAction2")
@ActionReferences({
    @ActionReference(path = "Menu/Plugins", position = 3333)
})
@Messages("CTL_TestAction2=Import Normal Data")
public final class TestAction2 implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        //Do something, display a message
        //NotifyDescriptor d = new NotifyDescriptor.Message("Hello...now trying to display a dialog", NotifyDescriptor.INFORMATION_MESSAGE);
        //DialogDisplayer.getDefault().notify(d);

        //Do something - for instance display a dialog
        //Dialogs API documentation: http://bits.netbeans.org/dev/javadoc/org-openide-dialogs/index.html?overview-summary.html
        //DialogDescriptor dd = new DialogDescriptor(new JPanel(), "My Dialog", false, null);
        //DialogDisplayer.getDefault().notify(dd);
        //what to do when dialog was closed 
        //if (dd.getValue () == DialogDescriptor.OK_OPTION) { 
            //ok button was pressed
            //Splunk splunk = new Splunk();
            //splunk.connection();
            HeadlessSimple hs = new HeadlessSimple();
            hs.script2();
        //} 
        //else{ 
            //cancel button was pressed
        //} 
  
    }
}