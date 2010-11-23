import tellapic

lj = {tellapic.LINE_JOIN_MITER:"Miter Join", tellapic.LINE_JOIN_ROUND:"Round Join", tellapic.LINE_JOIN_BEVEL:"Bevel Join"}
ec = {tellapic.END_CAPS_BUTT:"Butt caps", tellapic.END_CAPS_ROUND:"Round caps", tellapic.END_CAPS_SQUARE:"Square caps"}
tools = {tellapic.TOOL_MARKER:"Marker", tellapic.TOOL_PATH:"Path", tellapic.TOOL_ELLIPSE:"Ellipse", tellapic.TOOL_RECT:"Rectangle", tellapic.TOOL_TEXT:"Text", tellapic.TOOL_ERASER:"Eraser", tellapic.TOOL_PENCIL:"Pencil", tellapic.TOOL_LINE:"Line"}
fstyle = {tellapic.FONT_STYLE_NORMAL:"Normal", tellapic.FONT_STYLE_BOLD:"Bold", tellapic.FONT_STYLE_ITALIC:"Italic", tellapic.FONT_STYLE_BOLD_ITALIC:"Bold+Italic"}
events = {tellapic.EVENT_PRESS:"Press event", tellapic.EVENT_DRAG:"Drag event", tellapic.EVENT_RELEASE:"Release event", tellapic.EVENT_NULL:"Null event"}
fd = tellapic.connect_to("localhost", 50002)
if fd != -1:
    while True:
        print("\n\nConnected...waiting for header")
        header = tellapic.read_header_b(fd)
        if header.cbyte != tellapic.CTL_FAIL:
            print("Header read:", header.cbyte, "Reading stream of", header.ssize,"bytes")
            stream = tellapic.read_data_b(fd, header)
            if stream.header.cbyte == tellapic.CTL_CL_FIG:
                print("Data received:")
                print("\theader:", stream.header.cbyte)
                print("\tstream size:", stream.header.ssize)
                print("\tstream endian:", stream.header.endian)
                print("\t------------------")
                print("\tsender id:\t\t", stream.data.type.drawing.idfrom)
                print("\ttool used:\t\t", tools[stream.data.type.drawing.dcbyte & tellapic.TOOL_MASK])
                print("\tevent fired:\t\t", events[stream.data.type.drawing.dcbyte & tellapic.EVENT_MASK])
                print("\tpoint1:\t\t\t(",stream.data.type.drawing.point1.x, ",", stream.data.type.drawing.point1.y, ")")
                print("\tdrawing number:\t\t", stream.data.type.drawing.number)
                print("\twidth:\t\t\t", stream.data.type.drawing.width)
                print("\topacity:\t\t", stream.data.type.drawing.opacity)
                print("\tcolor:\t\t\t", stream.data.type.drawing.color.red, " ", stream.data.type.drawing.color.green, " ",stream.data.type.drawing.color.blue)                
                if stream.data.type.drawing.dcbyte & tellapic.TOOL_MASK == tellapic.TOOL_TEXT:
                    print("\tfont style:\t\t", fstyle[stream.data.type.drawing.type.text.style])
                    print("\tname length:\t\t", stream.data.type.drawing.type.text.namesize)
                    print("\tfont face:\t\t", stream.data.type.drawing.type.text.face)
                    print("\ttext: \t\t\t", stream.data.type.drawing.type.text.info)
                else:
                    print("\tpoint2:\t\t\t(",stream.data.type.drawing.type.figure.point2.x, ",", stream.data.type.drawing.type.figure.point2.y, ")")
                    print("\tend caps:\t\t", ec[stream.data.type.drawing.type.figure.endcaps])
                    print("\tline joins:\t\t", lj[stream.data.type.drawing.type.figure.linejoin])
                    print("\tmiter limit:\t\t", stream.data.type.drawing.type.figure.miterlimit)
                    print("\tdash phase:\t\t", stream.data.type.drawing.type.figure.dash_phase)
            else:
                print("Something went wrong with stream")
        else:
            print("Something went wrong with header")
            tellapic.close_fd(fd)
            break
else:
    print("Could not connect to server")
