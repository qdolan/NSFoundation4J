package ns.foundation;


public class NSMutableRange extends NSRange {
	private static final long serialVersionUID = 6985879405938984360L;

	public NSMutableRange() {
		super();
	}
	
	public NSMutableRange(int location, int length) {
		super(location, length);
	}
	
	public NSMutableRange(NSRange range) {
		super(range);
	}
	
	@Override
	public Object clone() {
		return new NSMutableRange(this);
	}
	
	public void intersectRange(NSRange otherRange) {
		NSRange newRange = this.rangeByIntersectingRange(otherRange);
		setLength(newRange.length);
		setLocation(newRange.location);
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public void setLocation(int location) {
		this.location = location;
	}
	
	public void unionRange(NSRange otherRange) {
		NSRange newRange = this.rangeByUnioningRange(otherRange);
		setLength(newRange.length);
		setLocation(newRange.location);
	}
	
}
