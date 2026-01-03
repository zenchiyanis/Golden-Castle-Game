package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

public class AnimatedMenuButton extends JButton {
	private static final long serialVersionUID = 1L;

    private final String titleText;
    private final String hoverText;
    private final String startText;

    private boolean hovering = false;
    private boolean started = false;

    private float borderProgress = 0f;
    private float textProgress = 0f;
    private float startProgress = 0f;

    private final Timer anim;

    public AnimatedMenuButton(String titleText, String hoverText, String startText) {
        super("");
        this.titleText = titleText;
        this.hoverText = hoverText;
        this.startText = startText;

        setPreferredSize(new Dimension(150, 50));
        setMinimumSize(new Dimension(150, 50));
        setMaximumSize(new Dimension(180, 60));

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setFont(getFont().deriveFont(Font.BOLD, 13f));

        anim = new Timer(16, e -> tick());
        anim.start();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovering = true; }
            @Override public void mouseExited(MouseEvent e) { hovering = false; }
        });
    }

    public void playStartAnimation() {
        started = true;
        startProgress = 0f;
    }

    private void tick() {
        float speed = 0.08f;

        float targetBorder = hovering ? 1f : 0f;
        borderProgress = approach(borderProgress, targetBorder, speed);

        float targetText = hovering ? 1f : 0f;
        textProgress = approach(textProgress, targetText, speed);

        if (started) {
            startProgress = approach(startProgress, 1f, 0.12f);
        } else {
            startProgress = approach(startProgress, 0f, 0.12f);
        }

        repaint();
    }

    private float approach(float v, float target, float step) {
        if (v < target) return Math.min(target, v + step);
        if (v > target) return Math.max(target, v - step);
        return v;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bgNormal = Color.WHITE;
        Color txtNormal = new Color(0x56, 0x8F, 0xA6);
        Color accent = new Color(0x44, 0xD8, 0xA4);

        boolean showStarted = started || startProgress > 0.001f;

        if (showStarted) {
            Color bg = mix(bgNormal, accent, startProgress);
            g2.setColor(bg);
        } else {
            g2.setColor(bgNormal);
        }
        g2.fillRoundRect(0, 0, w, h, 6, 6);

        if (showStarted) {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(2, 4, w - 4, h - 4, 6, 6);
        }

        g2.setColor(accent);

        int line = 2;
        int topY = 0;
        int botY = h - line;

        int horW = (int) (w * borderProgress);
        g2.fillRect(w - horW, topY, horW, line);
        g2.fillRect(0, botY, horW, line);

        int verH = (int) (h * borderProgress);
        g2.fillRect(w - line, 0, line, verH);
        g2.fillRect(0, h - verH, line, verH);

        if (showStarted) {
            g2.setColor(Color.WHITE);
            drawCentered(g2, startText, w, h, 0, 0, 0f);
        } else {
            g2.setColor(txtNormal);

            float t = textProgress;
            float titleY = lerp(h * 0.5f, -h * 0.5f, t);
            float titleRot = lerp(0f, (float) Math.toRadians(5), t);

            drawCentered(g2, titleText, w, h, 0, titleY - h * 0.5f, titleRot);

            g2.setColor(accent);
            float hoverY = lerp(h * 1.5f, h * 0.5f, t);
            drawCentered(g2, hoverText, w, h, 0, hoverY - h * 0.5f, 0f);
        }

        if (!isEnabled()) {
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(0, 0, w, h, 6, 6);
        }

        g2.dispose();
    }

    private void drawCentered(Graphics2D g2, String text, int w, int h, float dx, float dy, float rot) {
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int th = fm.getAscent();

        float cx = (w - tw) / 2f + dx;
        float cy = (h + th) / 2f + dy - 2f;

        AffineTransform old = g2.getTransform();
        g2.translate(w / 2f, h / 2f);
        g2.rotate(rot);
        g2.translate(-w / 2f, -h / 2f);
        g2.drawString(text, cx, cy);
        g2.setTransform(old);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private Color mix(Color a, Color b, float t) {
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(clamp(r), clamp(g), clamp(bl));
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
