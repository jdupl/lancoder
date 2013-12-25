package drfoliberg.master.listeners;

import drfoliberg.master.Node;

public interface INodeListener {

	void nodeAdded(Node n);
	void nodeDisconnected(Node n);
	void nodeRemoved(Node n);
	
}
