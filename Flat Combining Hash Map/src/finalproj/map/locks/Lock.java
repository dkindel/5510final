package finalproj.map.locks;

/**
 * General lock interface
 * @author dave
 *
 */
public interface Lock {
	public boolean lock();
	public boolean unlock();
}
