package com.vijet.ir.crawl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vijet.ir.model.Link;

public class LinkQueue {
	public static final int MAX_DEQUE_SIZE = 500;
	private List<Link> queue = new ArrayList<Link>();
	
	private Map<String,Set<String>> docInlinkSet;
	
	public LinkQueue(Map<String,Set<String>> docInlinkSet) {
		this.docInlinkSet = docInlinkSet;
	}
	
	public void enqueue(Link link){
		link.setWaitingTime(System.currentTimeMillis());
		queue.add(link);
	}
	
	public List<Link> dequeue(){
		proioritizeQueue();
		List<Link> set = new ArrayList<Link>();
		if(!queue.isEmpty()){
			while(set.size()!=MAX_DEQUE_SIZE){
				set.add(queue.remove(0));
			}
			return set;
		}
		return null;
	}
	
	public void proioritizeQueue(){
		Collections.sort(queue, new Comparator<Link>() {
			@Override
			public int compare(Link link1, Link link2) {
				if(link1.getIsRelevant() == link2.getIsRelevant()){
					if(docInlinkSet.get(link1.getCanonicalizedurl()).size() == docInlinkSet.get(link2.getCanonicalizedurl()).size()){
						return (link1.getWaitingTime()-link2.getWaitingTime()>0)?1:-1;
					}
					return docInlinkSet.get(link2.getCanonicalizedurl()).size() - docInlinkSet.get(link1.getCanonicalizedurl()).size();
				}
				return (link2.getIsRelevant()-link1.getIsRelevant());
			}
		});
	}
	
	public boolean hasLinks(){
		return queue.size()>0;
	}
	
	public int getSize(){
		return queue.size();
	}
	
	
}
