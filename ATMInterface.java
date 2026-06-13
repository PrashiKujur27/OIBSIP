import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ATMInterface extends JFrame {

    // ── PREMIUM FINTECH COLOR PALETTE (Neo-Bank Theme) ────────────────────────
    static final Color BG_BASE     = new Color(10, 14, 23);   
    static final Color BG_CARD     = new Color(20, 26, 40);   
    static final Color BG_INPUT    = new Color(14, 19, 31);   
    static final Color CYAN        = new Color(0, 229, 255);  
    static final Color PURPLE      = new Color(125, 42, 232); 
    static final Color SUCCESS     = new Color(30, 215, 96);  
    static final Color DANGER      = new Color(255, 60, 90);  
    static final Color TEXT_MAIN   = new Color(240, 245, 255);
    static final Color TEXT_MUTED  = new Color(130, 145, 170);
    static final Color BORDER_DIM  = new Color(40, 50, 70);

    static final DecimalFormat FMT = new DecimalFormat("₹#,##0.00");

    // ── DATA STRUCTURES ───────────────────────────────────────────────────────
    static class Account {
        String id, pin, name, acNo; double bal;
        List<Tx> hist = new ArrayList<>();
        Account(String id,String pin,String name,String acNo,double bal){
            this.id=id;this.pin=pin;this.name=name;this.acNo=acNo;this.bal=bal;
        }
    }
    static class Tx {
        enum T { DEP, WIT, OUT, IN, OPEN }
        T type; double amt, after; String note, date;
        Tx(T t,double amt,double after,String note){
            this.type=t;this.amt=amt;this.after=after;this.note=note;
            this.date=new SimpleDateFormat("dd MMM yyyy  HH:mm").format(new Date());
        }
        Color col(){ return(type==T.DEP||type==T.IN||type==T.OPEN)?SUCCESS:DANGER; }
        String sign(){ return(type==T.DEP||type==T.IN)?"+ ":(type==T.OPEN?"":"- "); }
        String icon(){ switch(type){case DEP:return"↓";case WIT:return"↑";case OUT:return"→";case OPEN:return"★";default:return"←";} }
        String label(){ switch(type){case DEP:return"Deposit";case WIT:return"Withdraw";case OUT:return"Transfer Out";case OPEN:return"Account Open";default:return"Transfer In";} }
    }

    // ── APP STATE ─────────────────────────────────────────────────────────────
    Map<String, Account> db = new HashMap<>(); 
    Account me = null;
    CardLayout CL; JPanel root;

    JLabel dName, dAcc, dBal, dLast, wBal, depBal, trBal;
    JTextField wAmt, depAmt, trTo, trAmt;
    JLabel wErr, depErr, trErr;
    DefaultTableModel histModel;

    public ATMInterface() {
        setTitle("NEXT-GEN ATM");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 650);
        setMinimumSize(new Dimension(750, 580));
        setLocationRelativeTo(null);

        CL = new CardLayout();
        root = new JPanel(CL);
        root.setBackground(BG_BASE);
        
        root.add(loginScreen(),    "login");
        root.add(registerScreen(), "register");
        root.add(dashScreen(),     "dash");
        root.add(withdrawScreen(), "withdraw");
        root.add(depositScreen(),  "deposit");
        root.add(transferScreen(), "transfer");
        root.add(historyScreen(),  "history");
        
        add(root);
        go("login");
    }

    void go(String name) {
        if (name.equals("dash")     && me!=null) refreshDash();
        if (name.equals("history")  && me!=null) refreshHistory();
        if (name.equals("withdraw") && me!=null && wBal  !=null) wBal.setText("Available Balance: "+FMT.format(me.bal));
        if (name.equals("deposit")  && me!=null && depBal!=null) depBal.setText("Available Balance: "+FMT.format(me.bal));
        if (name.equals("transfer") && me!=null && trBal !=null) trBal.setText("Available Balance: "+FMT.format(me.bal));
        CL.show(root, name);
    }

    // ─── LOGIN SCREEN ─────────────────────────────────────────────────────────

    JTextField loginId; JPasswordField loginPin; JLabel loginErr;

    JPanel loginScreen() {
        JPanel bg = grad(BG_BASE, new Color(5, 8, 15));
        bg.setLayout(new GridBagLayout());

        JPanel card = roundCard(24, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(pad(40,50,40,50));

        JPanel logo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,CYAN,getWidth(),getHeight(),PURPLE));
                g2.fillRoundRect(15, 5, 50, 50, 20, 20);
                g2.setPaint(new Color(255,255,255, 220));
                g2.fillRoundRect(25, 15, 30, 30, 10, 10);
            }
        };
        logo.setOpaque(false); logo.setMaximumSize(new Dimension(80, 60)); logo.setAlignmentX(CENTER_ALIGNMENT);
        card.add(logo); card.add(Box.createVerticalStrut(15));

        JLabel title = cLabel("NEXUS BANKING", 24, TEXT_MAIN, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title); card.add(Box.createVerticalStrut(5));

        JLabel sub = cLabel("Secure Digital ATM Interface", 13, TEXT_MUTED, SwingConstants.CENTER); sub.setAlignmentX(CENTER_ALIGNMENT);
        card.add(sub); card.add(Box.createVerticalStrut(35));

        card.add(fLbl("USER ID")); card.add(Box.createVerticalStrut(8));
        loginId = tField("Enter your ID"); card.add(loginId);
        card.add(Box.createVerticalStrut(18));

        card.add(fLbl("SECURE PIN")); card.add(Box.createVerticalStrut(8));
        loginPin = new JPasswordField(); styleF(loginPin,"••••••••"); card.add(loginPin);
        card.add(Box.createVerticalStrut(10));

        loginErr = new JLabel(" ", SwingConstants.CENTER); loginErr.setFont(new Font("Segoe UI",Font.PLAIN,13));
        loginErr.setForeground(DANGER); loginErr.setAlignmentX(CENTER_ALIGNMENT);
        card.add(loginErr); card.add(Box.createVerticalStrut(15));

        JButton btn = gBtn("SECURE LOGIN", CYAN, PURPLE, 48); btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(9999,48)); btn.addActionListener(e->doLogin()); loginPin.addActionListener(e->doLogin());
        card.add(btn); card.add(Box.createVerticalStrut(20));

        JButton createBtn = new JButton("No Account? Create One");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 13)); createBtn.setForeground(CYAN);
        createBtn.setContentAreaFilled(false); createBtn.setBorderPainted(false); createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createBtn.setAlignmentX(CENTER_ALIGNMENT);
        createBtn.addActionListener(e -> { loginErr.setText(" "); loginId.setText(""); loginPin.setText(""); go("register"); });
        card.add(createBtn);

        card.setPreferredSize(new Dimension(420, 560));
        bg.add(card);
        return bg;
    }

    void doLogin() {
        String id=loginId.getText().trim(), pin=new String(loginPin.getPassword()).trim();
        Account a=db.get(id);
        if(a==null||!a.pin.equals(pin)){loginErr.setText("✗  Invalid ID or PIN"); loginPin.setText(""); return;}
        me=a; loginErr.setText(" "); loginId.setText(""); loginPin.setText("");
        go("dash");
    }

    // ─── REGISTER SCREEN ──────────────────────────────────────────────────────

    JTextField regName, regId; JPasswordField regPin; JLabel regErr;

    JPanel registerScreen() {
        JPanel bg = grad(BG_BASE, new Color(5, 8, 15));
        bg.setLayout(new GridBagLayout());

        JPanel card = roundCard(24, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(pad(30,50,30,50));

        JLabel title = cLabel("Create Nexus Account", 22, CYAN, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI",Font.BOLD,22)); title.setAlignmentX(CENTER_ALIGNMENT);
        card.add(title); card.add(Box.createVerticalStrut(25));

        card.add(fLbl("FULL NAME")); card.add(Box.createVerticalStrut(8));
        regName = tField("e.g. Rahul Sharma"); card.add(regName);
        card.add(Box.createVerticalStrut(15));

        card.add(fLbl("CHOOSE USER ID")); card.add(Box.createVerticalStrut(8));
        regId = tField("Choose a unique ID"); card.add(regId);
        card.add(Box.createVerticalStrut(15));

        card.add(fLbl("CREATE PIN")); card.add(Box.createVerticalStrut(8));
        regPin = new JPasswordField(); styleF(regPin,"Enter a secure PIN"); card.add(regPin);
        card.add(Box.createVerticalStrut(10));

        regErr = new JLabel(" ", SwingConstants.CENTER); regErr.setFont(new Font("Segoe UI",Font.PLAIN,13));
        regErr.setForeground(DANGER); regErr.setAlignmentX(CENTER_ALIGNMENT);
        card.add(regErr); card.add(Box.createVerticalStrut(15));

        JButton btn = gBtn("CREATE ACCOUNT", CYAN, PURPLE, 48); btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(9999,48)); btn.addActionListener(e->doRegister());
        card.add(btn); card.add(Box.createVerticalStrut(15));

        JButton backBtn = new JButton("← Back to Login");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13)); backBtn.setForeground(TEXT_MUTED);
        backBtn.setContentAreaFilled(false); backBtn.setBorderPainted(false); backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> { regErr.setText(" "); regName.setText(""); regId.setText(""); regPin.setText(""); go("login"); });
        card.add(backBtn);

        card.setPreferredSize(new Dimension(420, 580));
        bg.add(card); return bg;
    }

    void doRegister() {
        String n = regName.getText().trim(), id = regId.getText().trim(), pin = new String(regPin.getPassword()).trim();
        if(n.isEmpty() || id.isEmpty() || pin.isEmpty()) { regErr.setText("✗ All fields are required."); return; }
        if(db.containsKey(id)) { regErr.setText("✗ User ID already exists."); return; }
        
        Account newAcc = new Account(id, pin, n, "NEXUS-" + (1000 + db.size() + 1), 0.0);
        newAcc.hist.add(new Tx(Tx.T.OPEN, 0.0, 0.0, "Account created")); db.put(id, newAcc);
        
        ok("Registration Successful", "Welcome, " + n + "!\nYour account is ready with ₹0.00 balance.\nPlease login to deposit funds.");
        regErr.setText(" "); regName.setText(""); regId.setText(""); regPin.setText(""); go("login");
    }

    // ─── DASHBOARD ────────────────────────────────────────────────────────────

    JPanel dashScreen() {
        JPanel p = grad(BG_BASE, BG_BASE); p.setLayout(new BorderLayout());

        JPanel hdr = new JPanel(new BorderLayout()); hdr.setBackground(BG_CARD); hdr.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_DIM), pad(15,30,15,30)));
        JLabel brand = cLabel("NEXUS BANKING", 16, CYAN, SwingConstants.LEFT); brand.setFont(new Font("Segoe UI",Font.BOLD,16));
        hdr.add(brand, BorderLayout.WEST); dName = cLabel("", 14, TEXT_MUTED, SwingConstants.RIGHT); hdr.add(dName, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS)); content.setBorder(pad(25,35,25,35));

        JPanel balCard = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,new Color(CYAN.getRed(),CYAN.getGreen(),CYAN.getBlue(), 40), getWidth(),getHeight(), new Color(PURPLE.getRed(),PURPLE.getGreen(),PURPLE.getBlue(), 40)));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
                g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 80));
                g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24);
            }
        };
        balCard.setOpaque(false); balCard.setBorder(pad(25,30,25,30)); balCard.setMaximumSize(new Dimension(9999,140));

        GridBagConstraints bg=new GridBagConstraints(); bg.anchor=GridBagConstraints.WEST;
        JLabel bTitle=cLabel("TOTAL BALANCE",12,CYAN,SwingConstants.LEFT); bTitle.setFont(new Font("Segoe UI",Font.BOLD,12));
        bg.gridy=0; bg.insets=new Insets(0,0,5,0); balCard.add(bTitle,bg);
        dBal=cLabel("₹0.00",48,TEXT_MAIN,SwingConstants.LEFT); dBal.setFont(new Font("Segoe UI",Font.BOLD,48));
        bg.gridy=1; bg.insets=new Insets(0,0,5,0); balCard.add(dBal,bg);
        dAcc=cLabel("",13,TEXT_MUTED,SwingConstants.LEFT); bg.gridy=2; balCard.add(dAcc,bg);

        content.add(balCard); content.add(Box.createVerticalStrut(15));
        dLast = cLabel("No transactions yet", 13, TEXT_MUTED, SwingConstants.LEFT); dLast.setBorder(pad(0,5,0,0)); dLast.setAlignmentX(LEFT_ALIGNMENT);
        content.add(dLast); content.add(Box.createVerticalStrut(25));

        JPanel grid = new JPanel(new GridLayout(2,2,18,18)); grid.setOpaque(false); grid.setMaximumSize(new Dimension(9999,240));
        grid.add(tile("↑","Withdraw","Take cash out",      DANGER,  e->go("withdraw")));
        grid.add(tile("↓","Deposit","Add funds",            SUCCESS, e->go("deposit")));
        grid.add(tile("→","Transfer","Send money",          CYAN,    e->go("transfer")));
        grid.add(tile("☰","History","View transactions",    PURPLE,  e->go("history")));
        content.add(grid);

        JScrollPane sp = new JScrollPane(content); sp.setOpaque(false); sp.getViewport().setOpaque(false); sp.setBorder(null);
        p.add(sp, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout()); foot.setBackground(BG_BASE); foot.setBorder(pad(15,30,20,30));
        JButton logout = new JButton("⏻  Secure Logout"); 
        logout.setFont(new Font("Segoe UI", Font.BOLD, 14)); logout.setForeground(DANGER);
        logout.setContentAreaFilled(false); logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e->{me=null; go("login");}); foot.add(logout, BorderLayout.EAST);
        p.add(foot, BorderLayout.SOUTH);
        return p;
    }

    void refreshDash() {
        if(me==null) return;
        dName.setText("👤  "+me.name); dBal.setText(FMT.format(me.bal)); dAcc.setText("A/C: " + me.acNo);
        List<Tx> h=me.hist;
        if(!h.isEmpty()){
            Tx t=h.get(h.size()-1);
            if(t.type == Tx.T.OPEN) { dLast.setText("Account active and ready"); dLast.setForeground(TEXT_MUTED); } 
            else { dLast.setText("Last: "+t.icon()+" "+t.sign()+FMT.format(t.amt).replace("-","")+" — "+t.note+" ("+t.date+")"); dLast.setForeground(t.col()); }
        }
    }

    JPanel tile(String icon,String title,String sub,Color accent,ActionListener a){
        JPanel t=new JPanel(new GridBagLayout()){
            boolean hov=false;
            { setCursor(new Cursor(Cursor.HAND_CURSOR)); addMouseListener(new MouseAdapter(){ public void mouseEntered(MouseEvent e){hov=true;repaint();} public void mouseExited(MouseEvent e){hov=false;repaint();} public void mouseClicked(MouseEvent e){a.actionPerformed(null);} }); }
            @Override protected void paintComponent(Graphics g0){
                Graphics2D g=(Graphics2D)g0; g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(hov ? new Color(BG_CARD.getRed(), BG_CARD.getGreen(), BG_CARD.getBlue(), 255) : BG_CARD);
                g.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g.setColor(hov ? accent : BORDER_DIM); g.setStroke(new BasicStroke(1.5f)); g.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g.setColor(accent); g.fillRoundRect(0,18,5,getHeight()-36,5,5);
            }
        };
        t.setOpaque(false); GridBagConstraints g=new GridBagConstraints(); g.anchor=GridBagConstraints.WEST;
        JLabel il=cLabel(icon,24,accent,SwingConstants.CENTER); il.setPreferredSize(new Dimension(45,45));
        g.gridx=0;g.gridy=0;g.gridheight=2;g.insets=new Insets(0,15,0,15); t.add(il,g);
        g.gridheight=1;g.gridx=1; JLabel tl=cLabel(title,16,TEXT_MAIN,SwingConstants.LEFT); tl.setFont(new Font("Segoe UI",Font.BOLD,16));
        g.gridy=0;g.insets=new Insets(0,0,2,0); t.add(tl,g);
        JLabel sl=cLabel(sub,12,TEXT_MUTED,SwingConstants.LEFT); g.gridy=1;g.insets=new Insets(0,0,0,0); t.add(sl,g); return t;
    }

    // ─── STRICT FORM BUILDER  ─────────────────────────────────────────────────

    void addRow(JPanel p, Component c, GridBagConstraints gc, int padTop) {
        gc.insets = new Insets(padTop, 0, 0, 0); p.add(c, gc); gc.gridy++;
    }

    // ─── WITHDRAW ─────────────────────────────────────────────────────────────

    JPanel withdrawScreen(){
        JPanel p=subShell("Withdraw Funds","↑",DANGER); JPanel form=(JPanel)((BorderLayout)p.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.anchor = GridBagConstraints.NORTH;

        wBal=balTag(DANGER);      addRow(form, wBal, gc, 0);
        addRow(form, fLbl("AMOUNT TO WITHDRAW (₹)"), gc, 20);
        wAmt=tField("Enter amount  (e.g. 500)"); addRow(form, wAmt, gc, 8);
        addRow(form, quickRow(new int[]{500,1000,2000,5000},wAmt,DANGER), gc, 12);
        wErr=errLbl();            addRow(form, wErr, gc, 10);
        
        JButton btn=gBtn("CONFIRM WITHDRAW",DANGER,DANGER.darker(),52); 
        btn.addActionListener(e->doWithdraw()); wAmt.addActionListener(e->doWithdraw());
        addRow(form, btn, gc, 10);
        
        gc.weighty = 1; addRow(form, Box.createGlue(), gc, 0); return p;
    }

    void doWithdraw(){
        double a=parseAmt(wAmt,wErr); if(a<0) return;
        if(me.bal == 0) {wErr.setText("✗  Your balance is ₹0. Please deposit funds first."); return;}
        if(a>me.bal){wErr.setText("✗  Insufficient balance.");return;}
        me.bal-=a; me.hist.add(new Tx(Tx.T.WIT,a,me.bal,"Cash withdrawal"));
        wAmt.setText(""); wErr.setText(" "); wBal.setText("Available Balance: "+FMT.format(me.bal));
        ok("Transaction Successful",FMT.format(a)+" withdrawn.\nNew balance: "+FMT.format(me.bal)); go("dash");
    }

    // ─── DEPOSIT ──────────────────────────────────────────────────────────────

    JPanel depositScreen(){
        JPanel p=subShell("Deposit Funds","↓",SUCCESS); JPanel form=(JPanel)((BorderLayout)p.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.anchor = GridBagConstraints.NORTH;

        depBal=balTag(SUCCESS);   addRow(form, depBal, gc, 0);
        addRow(form, fLbl("AMOUNT TO DEPOSIT (₹)"), gc, 20);
        depAmt=tField("Enter amount  (e.g. 1000)"); addRow(form, depAmt, gc, 8);
        addRow(form, quickRow(new int[]{500,1000,2000,5000},depAmt,SUCCESS), gc, 12);
        depErr=errLbl();          addRow(form, depErr, gc, 10);
        
        JButton btn=gBtn("CONFIRM DEPOSIT",SUCCESS,SUCCESS.darker(),52); 
        btn.addActionListener(e->doDeposit()); depAmt.addActionListener(e->doDeposit()); 
        addRow(form, btn, gc, 10);
        
        gc.weighty = 1; addRow(form, Box.createGlue(), gc, 0); return p;
    }

    void doDeposit(){
        double a=parseAmt(depAmt,depErr); if(a<0) return;
        me.bal+=a; me.hist.add(new Tx(Tx.T.DEP,a,me.bal,"Cash deposit"));
        depAmt.setText(""); depErr.setText(" "); depBal.setText("Available Balance: "+FMT.format(me.bal));
        ok("Deposit Successful",FMT.format(a)+" added to your account.\nNew balance: "+FMT.format(me.bal)); go("dash");
    }

    // ─── TRANSFER ─────────────────────────────────────────────────────────────

    JPanel transferScreen(){
        JPanel p=subShell("Transfer Funds","→",CYAN); JPanel form=(JPanel)((BorderLayout)p.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.anchor = GridBagConstraints.NORTH;

        trBal=balTag(CYAN);       addRow(form, trBal, gc, 0);
        addRow(form, fLbl("RECIPIENT USER ID"), gc, 20);
        trTo=tField("Enter Recipient's User ID"); addRow(form, trTo, gc, 8);
        addRow(form, fLbl("AMOUNT TO TRANSFER (₹)"), gc, 15);
        trAmt=tField("Enter amount"); addRow(form, trAmt, gc, 8);
        trErr=errLbl();           addRow(form, trErr, gc, 10);
        
        JButton btn=gBtn("SEND MONEY",CYAN,PURPLE,52); 
        btn.addActionListener(e->doTransfer()); 
        addRow(form, btn, gc, 10);
        
        gc.weighty = 1; addRow(form, Box.createGlue(), gc, 0); return p;
    }

    void doTransfer(){
        String toId=trTo.getText().trim(); double a=parseAmt(trAmt,trErr); if(a<0) return;
        if(toId.isEmpty()){trErr.setText("✗  Enter a recipient ID.");return;}
        if(toId.equals(me.id)){trErr.setText("✗  Cannot transfer to yourself.");return;}
        Account target=db.get(toId);
        if(target==null){trErr.setText("✗  Account not found: "+toId);return;}
        if(me.bal == 0) {trErr.setText("✗  Your balance is ₹0. Please deposit funds first."); return;}
        if(a>me.bal){trErr.setText("✗  Insufficient balance.");return;}
        
        me.bal-=a; target.bal+=a;
        me.hist.add(new Tx(Tx.T.OUT,a,me.bal,"To: "+target.name)); target.hist.add(new Tx(Tx.T.IN,a,target.bal,"From: "+me.name));
        trTo.setText(""); trAmt.setText(""); trErr.setText(" "); trBal.setText("Available Balance: "+FMT.format(me.bal));
        ok("Transfer Successful",FMT.format(a)+" sent to "+target.name+".\nNew balance: "+FMT.format(me.bal)); go("dash");
    }

    // ─── HISTORY ──────────────────────────────────────────────────────────────

    JPanel historyScreen(){
        JPanel p=subShell("Transaction Ledger","☰",PURPLE); JPanel center=(JPanel)((BorderLayout)p.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        center.setLayout(new BorderLayout()); center.setOpaque(false);

        String[] cols={"","Type","Amount","Balance After","Note","Date"};
        histModel=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        JTable tbl=new JTable(histModel){
            @Override public Component prepareRenderer(TableCellRenderer r,int row,int col){
                Component c=super.prepareRenderer(r,row,col); c.setBackground(row%2==0?BG_CARD:BG_BASE); c.setForeground(TEXT_MAIN);
                ((JComponent)c).setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
                if(col==2){String v=(String)getValueAt(row,col); c.setForeground(v!=null&&v.startsWith("+")?SUCCESS:DANGER);}
                return c;
            }
        };
        tbl.setBackground(BG_CARD); tbl.setForeground(TEXT_MAIN); tbl.setFont(new Font("Segoe UI",Font.PLAIN,14)); tbl.setRowHeight(44); tbl.setShowGrid(false); tbl.setIntercellSpacing(new Dimension(0,0)); tbl.setSelectionBackground(BORDER_DIM);
        tbl.getColumnModel().getColumn(0).setMaxWidth(40); tbl.getColumnModel().getColumn(2).setPreferredWidth(120); tbl.getColumnModel().getColumn(3).setPreferredWidth(130);

        JTableHeader hdr=tbl.getTableHeader(); hdr.setBackground(BG_BASE); hdr.setForeground(TEXT_MUTED); hdr.setFont(new Font("Segoe UI",Font.BOLD,12)); hdr.setPreferredSize(new Dimension(0,40)); hdr.setBorder(BorderFactory.createMatteBorder(0,0,2,0,BORDER_DIM));
        JScrollPane sp=new JScrollPane(tbl); sp.setBackground(BG_CARD); sp.getViewport().setBackground(BG_CARD); sp.setBorder(new RndBorder(16,BORDER_DIM)); sp.getVerticalScrollBar().setBackground(BG_BASE);
        center.add(sp, BorderLayout.CENTER); center.setBorder(pad(0,0,0,0)); return p;
    }

    void refreshHistory(){
        if(histModel==null||me==null) return; histModel.setRowCount(0);
        List<Tx> h=new ArrayList<>(me.hist); Collections.reverse(h);
        for(Tx t:h) {
            String displayAmt = t.type == Tx.T.OPEN ? "₹0.00" : t.sign()+FMT.format(t.amt).replace("-","");
            histModel.addRow(new Object[]{t.icon(),t.label(),displayAmt,FMT.format(t.after),t.note,t.date});
        }
    }

    // ─── SUB-SCREEN SHELL ─────────────────────────────────────────────────────

    JPanel subShell(String title, String icon, Color accent){
        JPanel root=grad(BG_BASE, BG_BASE); root.setLayout(new BorderLayout());
        JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(BG_CARD); hdr.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_DIM), pad(15,25,15,25)));
        
        JLabel back=new JLabel("←  Back to Dash"); back.setFont(new Font("Segoe UI",Font.BOLD,13)); back.setForeground(TEXT_MUTED); back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){go("dash");} public void mouseEntered(MouseEvent e){back.setForeground(TEXT_MAIN);} public void mouseExited(MouseEvent e){back.setForeground(TEXT_MUTED);}
        });
        hdr.add(back, BorderLayout.WEST); JLabel tl=new JLabel(icon+"  "+title, SwingConstants.CENTER); tl.setFont(new Font("Segoe UI",Font.BOLD,18)); tl.setForeground(accent); hdr.add(tl, BorderLayout.CENTER); root.add(hdr, BorderLayout.NORTH);
        
        JPanel formWrap=new JPanel(new GridBagLayout()); formWrap.setOpaque(false); formWrap.setBorder(pad(30,40,30,40));
        JPanel form=new JPanel(new GridBagLayout()); form.setOpaque(false);
        
        GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.weightx=1; g.anchor = GridBagConstraints.NORTH;
        formWrap.add(form,g); root.add(formWrap, BorderLayout.CENTER); return root;
    }

    // ─── UI COMPONENTS & STYLING ──────────────────────────────────────────────

    JLabel balTag(Color c){
        JLabel l=new JLabel(); l.setFont(new Font("Segoe UI",Font.BOLD,16)); l.setForeground(c); 
        l.setBorder(BorderFactory.createCompoundBorder(new RndBorder(12,new Color(c.getRed(),c.getGreen(),c.getBlue(),60)),pad(14,20,14,20))); 
        l.setOpaque(false); return l;
    }
    JLabel errLbl(){JLabel l=new JLabel(" ");l.setFont(new Font("Segoe UI",Font.PLAIN,13));l.setForeground(DANGER);return l;}
    JLabel fLbl(String t){JLabel l=new JLabel(t);l.setFont(new Font("Segoe UI",Font.BOLD,12));l.setForeground(TEXT_MUTED);return l;}
    JLabel cLabel(String t,int sz,Color c,int align){JLabel l=new JLabel(t,align);l.setFont(new Font("Segoe UI",Font.PLAIN,sz));l.setForeground(c);return l;}
    
    // BUG FIX 1: Removed hardcoded height restriction to prevent text clipping
    JTextField tField(String ph){JTextField f=new JTextField();styleF(f,ph);return f;}

    void styleF(JTextField f,String ph){
        f.setFont(new Font("Segoe UI",Font.PLAIN,16)); f.setForeground(TEXT_MAIN); f.setBackground(BG_INPUT); f.setCaretColor(CYAN);
        // Reduced padding slightly so it auto-sizes perfectly without clipping
        f.setBorder(BorderFactory.createCompoundBorder(new RndBorder(12,BORDER_DIM),pad(10,16,10,16))); 
        f.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent e){f.setBorder(BorderFactory.createCompoundBorder(new RndBorder(12,CYAN),pad(10,16,10,16)));}
            public void focusLost(FocusEvent e){f.setBorder(BorderFactory.createCompoundBorder(new RndBorder(12,BORDER_DIM),pad(10,16,10,16)));}
        });
    }

    JPanel quickRow(int[] amts, JTextField target, Color c){
        JPanel row=new JPanel(new GridLayout(1,amts.length,12,0)); row.setOpaque(false); row.setPreferredSize(new Dimension(0, 44));
        for(int a:amts){
            JButton b=new JButton("₹"+a){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover()?new Color(c.getRed(),c.getGreen(),c.getBlue(),40):BG_INPUT); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                    g2.setColor(getModel().isRollover()?c:BORDER_DIM); g2.setStroke(new BasicStroke(1.2f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                    super.paintComponent(g);
                } @Override protected void paintBorder(Graphics g){}
            };
            b.setFont(new Font("Segoe UI",Font.BOLD,13)); b.setForeground(TEXT_MAIN); b.setContentAreaFilled(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addActionListener(e->target.setText(String.valueOf(a))); row.add(b);
        }
        return row;
    }

    JButton gBtn(String text, Color c1, Color c2, int h){
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,getModel().isRollover()?c1.brighter():c1,getWidth(),getHeight(),getModel().isRollover()?c2.brighter():c2));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14); super.paintComponent(g);
            } @Override protected void paintBorder(Graphics g){}
        };
        b.setFont(new Font("Segoe UI",Font.BOLD,15)); b.setForeground(Color.WHITE); b.setContentAreaFilled(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setPreferredSize(new Dimension(0,h)); return b;
    }

    void ok(String title, String msg){
        JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setBackground(BG_CARD); p.setBorder(pad(20,25,20,25));
        JLabel t=new JLabel(title);t.setFont(new Font("Segoe UI",Font.BOLD,18));t.setForeground(CYAN);p.add(t); p.add(Box.createVerticalStrut(15));
        for(String line:msg.split("\n")){JLabel l=new JLabel(line);l.setFont(new Font("Segoe UI",Font.PLAIN,14));l.setForeground(TEXT_MAIN);p.add(l);}
        
        // Ensure the dialog uses dark theme backgrounds
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("Panel.background", BG_CARD); 
        
        JOptionPane.showMessageDialog(this,p,"System Message",JOptionPane.PLAIN_MESSAGE);
    }

    double parseAmt(JTextField f,JLabel err){
        try{double v=Double.parseDouble(f.getText().trim().replace(",","").replace("₹","").replace("$",""));
            if(v<=0){err.setText("✗  Amount must be greater than zero.");return -1;} err.setText(" ");return v;}
        catch(NumberFormatException e){err.setText("✗  Please enter a valid number.");return -1;}
    }

    JPanel grad(Color c1, Color c2){return new JPanel(){ {setOpaque(true);} @Override protected void paintComponent(Graphics g){super.paintComponent(g); ((Graphics2D)g).setPaint(new GradientPaint(0,0,c1,0,getHeight(),c2)); ((Graphics2D)g).fillRect(0,0,getWidth(),getHeight());}};}
    JPanel roundCard(int r, Color bg){return new JPanel(){ {setOpaque(false);} @Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(bg);g2.fillRoundRect(0,0,getWidth(),getHeight(),r,r);}};}
    EmptyBorder pad(int t,int l,int b,int r){return new EmptyBorder(t,l,b,r);}

    static class RndBorder extends AbstractBorder {
        int r; Color c; RndBorder(int r,Color c){this.r=r;this.c=c;}
        @Override public void paintBorder(Component comp,Graphics g,int x,int y,int w,int h){Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(c);g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(x,y,w-1,h-1,r,r);}
        @Override public Insets getBorderInsets(Component c){return new Insets(r/2,r/2,r/2,r/2);} @Override public Insets getBorderInsets(Component c,Insets i){i.set(r/2,r/2,r/2,r/2);return i;}
    }

    // ─── RUN APP ──────────────────────────────────────────────────────────────
    public static void main(String[] args){
        try { 
            // BUG FIX 2: Use Cross-Platform LookAndFeel to prevent native Windows from overriding button colors to white!
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); 
        } catch(Exception ignored){}
        
        // Explicitly style the standard JOptionPane buttons to match the dark theme
        UIManager.put("Button.background", BG_INPUT); 
        UIManager.put("Button.foreground", TEXT_MAIN); 
        UIManager.put("Button.focus", new Color(0,0,0,0)); // Removes the ugly dotted line on focus
        
        SwingUtilities.invokeLater(()->new ATMInterface().setVisible(true));
    }
}