package org.peakaboo.framework.swidget.dialogues.fileio.breadcrumb;

import java.io.File;
import java.util.function.Function;

import org.peakaboo.framework.swidget.dialogues.fileio.places.Place;
import org.peakaboo.framework.swidget.dialogues.fileio.places.Places;
import org.peakaboo.framework.swidget.widgets.breadcrumb.BreadCrumb;
import org.peakaboo.framework.swidget.widgets.breadcrumb.BreadCrumbEntry;
import org.peakaboo.framework.swidget.widgets.buttons.ToggleImageButton;

public class FileBreadCrumbEntry extends BreadCrumbEntry<File> {

	public FileBreadCrumbEntry(BreadCrumb<File> parent, File item, Function<File, String> formatter) {
		super(parent, item, formatter);
	}

	@Override
	protected ToggleImageButton make() {
		Place dir = Places.forPlatform().get(getItem());
		ToggleImageButton button = super.make();
		if (dir != null && dir.isRoot()) {
			//not a good idea -- the ImageButton may regenerate it's UI based on internal state 
			button.setIcon(dir.getIcon());
		}
		return button;
	}
	
}
