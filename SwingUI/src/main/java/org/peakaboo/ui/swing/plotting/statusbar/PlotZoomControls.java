package org.peakaboo.ui.swing.plotting.statusbar;

import java.awt.BorderLayout;

import javax.swing.JPopupMenu;

import org.peakaboo.controller.plotter.PlotController;
import org.peakaboo.framework.swidget.icons.StockIcon;
import org.peakaboo.framework.swidget.widgets.ClearPanel;
import org.peakaboo.framework.swidget.widgets.Spacing;
import org.peakaboo.framework.swidget.widgets.ZoomSlider;
import org.peakaboo.framework.swidget.widgets.buttons.ImageButton;
import org.peakaboo.framework.swidget.widgets.buttons.ImageButtonLayout;
import org.peakaboo.framework.swidget.widgets.buttons.ToggleImageButton;

public class PlotZoomControls extends ImageButton {
	
	private PlotController controller;
	
	private ZoomSlider zoomSlider;
	private ClearPanel zoomPanel;
	
	public PlotZoomControls(PlotController controller) {
		super(StockIcon.FIND);
		super.withTooltip("Zoom")
			.withLayout(ImageButtonLayout.IMAGE)
			.withBordered(false);
		
		this.controller = controller;
		
		zoomPanel = new ClearPanel();
		zoomPanel.setBorder(Spacing.bMedium());
		
		zoomSlider = new ZoomSlider(10, 1000, 10, value -> {
			controller.view().setZoom(value / 100f);
		});
		zoomSlider.setOpaque(false);
		zoomSlider.setValue(100);
		zoomPanel.add(zoomSlider, BorderLayout.CENTER);

		
		final ToggleImageButton lockHorizontal = new ToggleImageButton("", StockIcon.MISC_LOCKED).withTooltip("Lock Vertical Zoom to Window Size");
		lockHorizontal.setSelected(true);
		lockHorizontal.addActionListener(e -> {
			controller.view().setLockPlotHeight(lockHorizontal.isSelected());
		});
		zoomPanel.add(lockHorizontal, BorderLayout.EAST);
		
		JPopupMenu zoomMenu = new JPopupMenu();
		zoomMenu.setBorder(Spacing.bNone());
		zoomMenu.add(zoomPanel);
		
		this.addActionListener(e -> {
			zoomMenu.show(this, (int)((-zoomMenu.getPreferredSize().getWidth()+this.getSize().getWidth())/2f), (int)-zoomMenu.getPreferredSize().getHeight());
		});
		
	}
	
	void setWidgetState(boolean hasData) {
		this.setEnabled(hasData);
		zoomSlider.setValueEventless((int)(controller.view().getZoom()*100));
		
	}
	
}
