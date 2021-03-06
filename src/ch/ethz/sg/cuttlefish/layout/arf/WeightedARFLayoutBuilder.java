package ch.ethz.sg.cuttlefish.layout.arf;

import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;

public class WeightedARFLayoutBuilder implements LayoutBuilder {

	private boolean incremental = true;
	private boolean keepInitialPositions = false;
	private final boolean fixedThreshold = true;

	@Override
	public WeightedARFLayout buildLayout() {
		WeightedARFLayout arf = new WeightedARFLayout(this, incremental,
				keepInitialPositions);
		arf.setFixedThreshold(fixedThreshold);

		return arf;
	}

	@Override
	public String getName() {
		return "Weighted ARF";
	}

	@Override
	public LayoutUI getUI() {
		// No LayoutUI needed in Cuttlefish
		return null;
	}

	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
	}

	public void keepInitialPositions(boolean keep) {
		this.keepInitialPositions = keep;
	}

}
