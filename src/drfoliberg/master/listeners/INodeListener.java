package drfoliberg.master.listeners;

import java.util.EventListener;

import drfoliberg.common.Node;

public interface INodeListener extends EventListener {

	void nodeAdded(Node n);
	void nodeDisconnected(Node n);
	void nodeRemoved(Node n);
	
}
