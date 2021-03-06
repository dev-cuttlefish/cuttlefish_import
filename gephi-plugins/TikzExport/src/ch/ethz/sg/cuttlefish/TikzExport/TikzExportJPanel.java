/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TikzExportPanel.java
 *
 * Created on Dec 28, 2011, 1:57:28 PM
 */
package ch.ethz.sg.cuttlefish.TikzExport;

import javax.swing.JPanel;
import org.gephi.io.exporter.spi.Exporter;

/**
 *
 * @author ptsankov
 */
public class TikzExportJPanel extends JPanel {

    /** Creates new form TikzExportPanel */
    public TikzExportJPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodeScalingFactorLabel = new javax.swing.JLabel();
        edgeScalingFactorLabel = new javax.swing.JLabel();
        xYScalingFactorLabel = new javax.swing.JLabel();
        nodeFactor = new javax.swing.JTextField();
        edgeFactor = new javax.swing.JTextField();
        coorFactor = new javax.swing.JTextField();
        hideEdgeLabels = new javax.swing.JCheckBox();
        hideNodeLabels = new javax.swing.JCheckBox();
        ballEffect = new javax.swing.JCheckBox();
        defaultValues = new javax.swing.JToggleButton();

        nodeScalingFactorLabel.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.nodeScalingFactorLabel.text")); // NOI18N

        edgeScalingFactorLabel.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.edgeScalingFactorLabel.text")); // NOI18N

        xYScalingFactorLabel.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.xYScalingFactorLabel.text")); // NOI18N

        nodeFactor.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.nodeFactor.text")); // NOI18N

        edgeFactor.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.edgeFactor.text")); // NOI18N
        edgeFactor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edgeFactorActionPerformed(evt);
            }
        });

        coorFactor.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.coorFactor.text")); // NOI18N

        hideEdgeLabels.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.hideEdgeLabels.text")); // NOI18N

        hideNodeLabels.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.hideNodeLabels.text")); // NOI18N

        ballEffect.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.ballEffect.text")); // NOI18N

        defaultValues.setText(org.openide.util.NbBundle.getMessage(TikzExportJPanel.class, "TikzExportJPanel.defaultValues.text")); // NOI18N
        defaultValues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultValuesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ballEffect)
                    .addComponent(hideEdgeLabels)
                    .addComponent(hideNodeLabels)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(edgeScalingFactorLabel)
                            .addComponent(nodeScalingFactorLabel)
                            .addComponent(xYScalingFactorLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nodeFactor, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(edgeFactor, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(coorFactor, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(24, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(117, Short.MAX_VALUE)
                .addComponent(defaultValues)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(hideNodeLabels)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(hideEdgeLabels)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ballEffect)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeScalingFactorLabel)
                    .addComponent(nodeFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeScalingFactorLabel)
                    .addComponent(edgeFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(coorFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xYScalingFactorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addComponent(defaultValues)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void edgeFactorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edgeFactorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_edgeFactorActionPerformed

    private void defaultValuesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultValuesActionPerformed
        hideEdgeLabels.setSelected(false);
        hideNodeLabels.setSelected(false);
        ballEffect.setSelected(false);
        edgeFactor.setText(Double.toString(0.5));
        nodeFactor.setText(Double.toString(0.5));
        coorFactor.setText(Double.toString(0.01));
    }//GEN-LAST:event_defaultValuesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox ballEffect;
    private javax.swing.JTextField coorFactor;
    private javax.swing.JToggleButton defaultValues;
    private javax.swing.JTextField edgeFactor;
    private javax.swing.JLabel edgeScalingFactorLabel;
    private javax.swing.JCheckBox hideEdgeLabels;
    private javax.swing.JCheckBox hideNodeLabels;
    private javax.swing.JTextField nodeFactor;
    private javax.swing.JLabel nodeScalingFactorLabel;
    private javax.swing.JLabel xYScalingFactorLabel;
    // End of variables declaration//GEN-END:variables

    public void setup(TikzExport exporter) {
        ballEffect.setSelected(exporter.nodeStyle().compareToIgnoreCase("ball") == 0);
        hideEdgeLabels.setSelected(exporter.hideEdgeLabels());
        hideNodeLabels.setSelected(exporter.hideNodeLabels());
        nodeFactor.setText(Double.toString(exporter.nodeScalingFactor()));
        edgeFactor.setText(Double.toString(exporter.edgeScalingFactor()));
        coorFactor.setText(Double.toString(exporter.coordinatesScalingFactor()));
    }

    void unsetup(TikzExport exporter) {
        exporter.setHideEdgeLabels(hideEdgeLabels.isSelected());
        exporter.setHideNodeLabels(hideNodeLabels.isSelected());
        exporter.setNodeStyle(ballEffect.isSelected() ? "ball" : "circle");
        exporter.setNodeScalingFactor(Double.parseDouble(nodeFactor.getText()));
        exporter.setEdgeScalingFactor(Double.parseDouble(edgeFactor.getText()));
        exporter.setCoordinatesScalingFactor(Double.parseDouble(coorFactor.getText()));
    }
}
