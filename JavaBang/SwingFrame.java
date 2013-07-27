#!./javabang

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class SwingFrame extends JFrame
{
	public SwingFrame()
	{
	   setTitle("Simple Swing Frame");
	   setSize(300, 200);
	   setLocationRelativeTo(null);
	   setDefaultCloseOperation(EXIT_ON_CLOSE);		   
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater
		(
			new Runnable()
			{
				@Override
				public void run()
				{
					SwingFrame ex = new SwingFrame();
					ex.setVisible(true);
				}
			}
		);
	}
}
