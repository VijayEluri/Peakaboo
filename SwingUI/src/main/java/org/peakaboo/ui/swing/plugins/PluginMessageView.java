package org.peakaboo.ui.swing.plugins;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.peakaboo.framework.swidget.Swidget;
import org.peakaboo.framework.swidget.widgets.CenteringPanel;
import org.peakaboo.framework.swidget.widgets.Spacing;

public class PluginMessageView extends JPanel {

	public PluginMessageView(String message, int width) {
		setLayout(new BorderLayout());
		JLabel label = new JLabel();
		label.setText(Swidget.lineWrapHTML(label, message, 350));
		label.setForeground(Color.GRAY);
		CenteringPanel panel = new CenteringPanel(label);
		add(panel, BorderLayout.CENTER);
		setBorder(Spacing.bHuge());
	}
	
}
