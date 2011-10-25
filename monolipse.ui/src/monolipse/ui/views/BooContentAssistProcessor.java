/**
 * 
 */
package monolipse.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import monolipse.core.compiler.AbstractBooServiceClient;
import monolipse.core.compiler.CompilerProposal;
import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;
import monolipse.ui.TextViewerUtilities;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;


public class BooContentAssistProcessor implements IContentAssistProcessor {
	
	Map<String, Image> _imageMap = new HashMap<String, Image>();
	private CompilerProposal[] _cachedProposals;
	private String _cachedLine;
	private AbstractBooServiceClient _client;
	
	public BooContentAssistProcessor(AbstractBooServiceClient client) {
		_client = client;
		mapImage("Class", IBooUIConstants.CLASS);
		mapImage("Method", IBooUIConstants.METHOD);
		mapImage("Constructor", IBooUIConstants.METHOD);
		mapImage("Field", IBooUIConstants.FIELD);
		mapImage("Property", IBooUIConstants.PROPERTY);
		mapImage("Event", IBooUIConstants.EVENT);
		mapImage("Namespace", IBooUIConstants.NAMESPACE);
		mapImage("Interface", IBooUIConstants.INTERFACE);
		mapImage("Callable", IBooUIConstants.CALLABLE);
		mapImage("Struct", IBooUIConstants.STRUCT);
		mapImage("Enum", IBooUIConstants.ENUM);
	}
	
	void mapImage(String entityType, String key) {
		_imageMap.put(entityType, BooUI.getImage(key));
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		String line = TextViewerUtilities.getLineAtOffset(viewer, offset);
		
		ICompletionProposal[] proposals = getFromCache(line, offset);
		if (null != proposals) return proposals;
		
		try {
			CompilerProposal[] found = _client.getCompletionProposals(getCompletionText(viewer, offset));
			proposals = newCompletionProposalArray("", offset, found);
			_cachedLine = line;
			_cachedProposals = found;
			return proposals;
		} catch (IOException e) {
			BooUI.logException(e);
		}
		return new ICompletionProposal[0];
	}
	
	protected String getCompletionText(ITextViewer viewer, int offset) {
		 return TextViewerUtilities.getLineAtOffset(viewer, offset) + "__codecomplete__";
	}

	private ICompletionProposal[] newCompletionProposalArray(String existingPrefix, int offset, CompilerProposal[] found) {
		ICompletionProposal[] proposals;
		proposals = new ICompletionProposal[found.length];
		for (int i=0; i<found.length; ++i) {
			proposals[i] = newCompletionProposal(existingPrefix, offset, found[i]);
		}
		return proposals;
	}

	private CompletionProposal newCompletionProposal(String existingPrefix, int offset, CompilerProposal proposal) {
		String name = proposal.getName();
		final String description = isMember(proposal.getEntityType())
			? proposal.getDescription()
			: name;
		String completion = name.substring(existingPrefix.length());
		CompletionProposal completionProposal = new CompletionProposal(completion, offset, 0, completion.length(), getImage(proposal), massageDescription(description), null, description);
		return completionProposal;
	}

	private boolean isMember(String entityType) {
		return entityType.equals("Method")
		|| entityType.equals("Field")
		|| entityType.equals("Property")
		|| entityType.equals("Event");
	}

	private ICompletionProposal[] getFromCache(String line, int offset) {
		if (null == _cachedLine) return null;
		if (line.endsWith(".")) {
			return line.equals(_cachedLine) ? newCompletionProposalArray("", 0, _cachedProposals) : null;
		}
		int lastDot = line.lastIndexOf('.');
		String prefix = line.substring(0, lastDot+1);
		if (!_cachedLine.equals(prefix)) return null;
		return filterCachedProposals(line.substring(lastDot+1), offset);
	}

	private ICompletionProposal[] filterCachedProposals(String prefix, int offset) {
		ArrayList<CompletionProposal> filtered = new ArrayList<CompletionProposal>();
		for (int i=0; i<_cachedProposals.length; ++i) {
			CompilerProposal item = _cachedProposals[i];
			String name = item.getName();
			if (name.startsWith(prefix)) {
				filtered.add(newCompletionProposal(prefix, offset, item));
			}
		}
		return filtered.toArray(new ICompletionProposal[filtered.size()]);
	}

	private Image getImage(CompilerProposal proposal) {
		return _imageMap.get(proposal.getEntityType());
	}

	private String massageDescription(String description) {
		return removePrefix(removePrefix(removePrefix(description, "static "), "event "), "def ");
	}
	
	private String removePrefix(String s, String prefix) {
		if (s.startsWith(prefix)) return s.substring(prefix.length());
		return s;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return new char[0];
	}

	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}
	
}