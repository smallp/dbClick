package me.small.vision.window

import com.intellij.ide.BrowserUtil
import me.small.vision.data.InfoPO
import me.small.vision.data.VisionInfo
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class DBInfo(private val set: VisionInfo) : MouseListener {
    private var panel1: JPanel? = null
    private var dbList: JTree? = null
    private var refresh: JButton? = null
    private var updateState: JLabel? = null
    private val popupMenu: JPopupMenu
    private var url = ""

    val content get() = panel1

    init {
        refresh?.addActionListener {
            set.parse()
            drawTree()
        }
        dbList!!.cellRenderer = MyTreeCellRenderer()
        dbList!!.addMouseListener(this)
        dbList!!.isRootVisible = false
        drawTree()
        popupMenu = JPopupMenu()
        popupMenu.add(JMenuItem("name").apply { isEnabled = false })
        popupMenu.addSeparator()
        popupMenu.add(JMenuItem("stable version").apply { isEnabled = false })
        popupMenu.add(JMenuItem("preview version").apply { isEnabled = false })
        popupMenu.add(JMenuItem("testing version").apply { isEnabled = false })
        val readme = JMenuItem("readme")
        readme.addActionListener {
            openReadme()
        }
        popupMenu.add(readme)
    }

    private fun drawTree() {
        val data = set.parsedData
        val res = DefaultMutableTreeNode("root")
        val sorted = data.keys.toSortedSet()
        sorted.forEach { k ->
            val v = data[k]!!
            val item = DefaultMutableTreeNode(k)
            v.keys.sorted().forEach {
                item.add(DefaultMutableTreeNode(v[it]))
            }
            res.add(item)
        }
        dbList!!.model = DefaultTreeModel(res, false)

        val update = set.isUpdate()
        updateState!!.text = if (update) "当前自动更新依赖" else "当前已冻结依赖"
    }

    private fun openReadme() {
        BrowserUtil.browse(url)
    }

    override fun mouseClicked(e: MouseEvent) {
    }

    override fun mousePressed(e: MouseEvent) {
        showMenu(e)
    }

    override fun mouseReleased(e: MouseEvent) {
        showMenu(e)
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }

    private fun showMenu(e: MouseEvent) {
        if (e.isPopupTrigger) {
            val res = dbList!!.getPathForLocation(e.x, e.y)?.path ?: return
            if (res.size != 3) return
            val item = ((res[2] as DefaultMutableTreeNode).userObject as InfoPO).name
            val model = set.parsedData[res[1].toString()]?.get(item) ?: return
            (popupMenu.getComponent(0) as JMenuItem).text = model.name
            model.otherVersion.forEachIndexed { index, s ->
                (popupMenu.getComponent(index + 2) as JMenuItem).text = s.trim()
            }
            url = model.readme
            (popupMenu.getComponent(5) as JMenuItem).isEnabled = url.isNotEmpty()
            popupMenu.show(e.component, e.x, e.y)
        }
    }
}