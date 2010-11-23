public class javatest {
    static {
	System.loadLibrary("javatest");
    }
    public static String ec[] = new String[] { "Butt caps", "Round caps", "Square caps"};
    public static String lj[] = new String[] { "Miter Join", "Round Join", "Bevel Join"};
    public static String fstyle[] = new String[] {"Normal", "Bold", "Italic", "Bold+Italic"};
    public static String tools[] = new String[] {
	"Marker",
	"Path",
	"Ellipse",
	"Rectangle",
	"Text",
	"Eraser",
	"Pencil",
	"Line"
    };
    public static String events[] = new String[] {
	"Null Event",
	"Press Event",
	"Drag Event",
	"Release Event"
    };

    public static void main(String arg[]) {
	int fd = test.connect_to("localhost", 50002);
	if (fd != -1) {
	    System.out.println("Connected.");
	    header_t header = test.read_header_b(fd);
	    if (header != null) {
		System.out.println("Header read: "+header.getCbyte()+" ("+header.getSsize()+" bytes)");
		stream_t stream = test.read_data_b(fd, header);
		if (stream != null) {
		    System.out.println("Stream read.");
		    if (header.getCbyte() == testConstants.CTL_CL_FIG) {
			System.out.println("Data received:");
			System.out.println("\theader:"+stream.getHeader().getCbyte());
			System.out.println("\tstream size:"+stream.getHeader().getSsize());
			System.out.println("\tstream endian:"+stream.getHeader().getEndian());
			System.out.println("\t------------------");
			System.out.println("\tsender id:\t\t"+stream.getData().getType().getDrawing().getIdfrom());
			System.out.println("\ttool used:\t\t"+tools[((stream.getData().getType().getDrawing().getDcbyte() & testConstants.TOOL_MASK)>>4)-1]);
			System.out.println("\tevent fired:\t\t"+events[(stream.getData().getType().getDrawing().getDcbyte() & testConstants.EVENT_MASK)>>2]);
                
			if ((stream.getData().getType().getDrawing().getDcbyte() & testConstants.TOOL_MASK) == testConstants.TOOL_TEXT) {
			    System.out.println("\tpoint:\t\t\t("+stream.getData().getType().getDrawing().getType().getText().getPoint().getX()+","+stream.getData().getType().getDrawing().getType().getText().getPoint().getY()+")");
			    System.out.println("\tdrawing number:\t\t"+stream.getData().getType().getDrawing().getType().getText().getNumber());
			    System.out.println("\twidth:\t\t\t"+stream.getData().getType().getDrawing().getType().getText().getWidth());
			    System.out.println("\topacity:\t\t"+stream.getData().getType().getDrawing().getType().getText().getOpacity());
			    System.out.println("\tcolor:\t\t\t"+stream.getData().getType().getDrawing().getType().getText().getColor().getRed()+" "+stream.getData().getType().getDrawing().getType().getText().getColor().getGreen()+" "+stream.getData().getType().getDrawing().getType().getText().getColor().getBlue());
			    System.out.println("\tfont style:\t\t"+fstyle[stream.getData().getType().getDrawing().getType().getText().getStyle()]);
			    System.out.println("\tname length:\t\t"+stream.getData().getType().getDrawing().getType().getText().getNamesize());
			    System.out.println("\tfont face:\t\t"+stream.getData().getType().getDrawing().getType().getText().getFace());
			    System.out.println("\ttext: \t\t\t"+stream.getData().getType().getDrawing().getType().getText().getInfo());
			} else {
			    System.out.println("\tpoint1:\t\t\t("+stream.getData().getType().getDrawing().getType().getFigure().getPoint1().getX()+","+stream.getData().getType().getDrawing().getType().getFigure().getPoint1().getY()+")");
			    System.out.println("\tpoint2:\t\t\t("+stream.getData().getType().getDrawing().getType().getFigure().getPoint2().getX()+","+stream.getData().getType().getDrawing().getType().getFigure().getPoint2().getY()+")");
			    System.out.println("\tdrawing number:\t\t"+stream.getData().getType().getDrawing().getType().getFigure().getNumber());
			    System.out.println("\twidth:\t\t\t"+stream.getData().getType().getDrawing().getType().getFigure().getWidth());
			    System.out.println("\topacity:\t\t"+stream.getData().getType().getDrawing().getType().getFigure().getOpacity());
			    System.out.println("\tcolor:\t\t\t"+stream.getData().getType().getDrawing().getType().getFigure().getColor().getRed()+" "+stream.getData().getType().getDrawing().getType().getFigure().getColor().getGreen()+" "+stream.getData().getType().getDrawing().getType().getFigure().getColor().getBlue());
			    System.out.println("\tend caps:\t\t"+ec[stream.getData().getType().getDrawing().getType().getFigure().getEndcaps()]);
			    System.out.println("\tline joins:\t\t"+lj[stream.getData().getType().getDrawing().getType().getFigure().getLinejoin()]);
			    System.out.println("\tmiter limit:\t\t"+stream.getData().getType().getDrawing().getType().getFigure().getMiterlimit());
			    System.out.println("\tdash phase:\t\t"+stream.getData().getType().getDrawing().getType().getFigure().getDash_phase());
			}
		    }
		} else {
		    System.out.println("Something went wrong while reading stream.");
		}
	    } else {
		System.out.println("Something went wrong while reading header");
	    }
	}
	test.close_fd(fd);
    }
}