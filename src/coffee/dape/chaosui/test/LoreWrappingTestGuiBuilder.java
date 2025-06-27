package coffee.dape.chaosui.test;

import org.bukkit.Material;
import org.bukkit.inventory.InventoryView;

import coffee.dape.chaosui.ChaosBuilder;
import coffee.dape.chaosui.ChaosFactory.InvTemplate;
import coffee.dape.chaosui.anno.ChaosGUI;
import coffee.dape.chaosui.components.buttons.Button;
import coffee.dape.chaosui.interfaces.common.DefaultCI;
import coffee.dape.utils.ColourUtils;
import coffee.dape.utils.GradientUtils;
import coffee.dape.utils.HeadUtils;
import coffee.dape.utils.ItemBuilder;
import coffee.dape.utils.StdCols;

@ChaosGUI(name = "LoreWrappingTest",handler = TestChaosGuiHandler.class,template = InvTemplate.CHEST_6)
public class LoreWrappingTestGuiBuilder extends ChaosBuilder
{
	@Override
	public void init()
	{
		setInterface(new DefaultCI());
		
		putStaticComponent(new Button(4,ItemBuilder.of(HeadUtils.INFO_ICON.clone())
				.name("Information",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(ColourUtils.applyColour("Testing how lore wraps around to multiple lines",ColourUtils.TEXT))
				.create()));
		
		putStaticComponent(new Button(9,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, no colour, default length of 60",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",60)
				.create()));
		
		putStaticComponent(new Button(10,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, no colour, length of 40",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",40)
				.create()));
		
		putStaticComponent(new Button(11,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, no colour, length of 20",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",20)
				.create()));
		
		putStaticComponent(new Button(18,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, custom colour, default length of 60",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(ColourUtils.applyColour("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",ColourUtils.MELON),60)
				.create()));
		
		putStaticComponent(new Button(19,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, custom colour, length of 40",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(ColourUtils.applyColour("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",ColourUtils.MELON),40)
				.create()));
		
		putStaticComponent(new Button(20,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, custom colour, length of 20",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(ColourUtils.applyColour("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",ColourUtils.MELON),20)
				.create()));
		
		putStaticComponent(new Button(27,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, gradient, default length of 60",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(GradientUtils.applyGradient("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",GradientUtils.SAND_TO_BLUE_REVERSE),60)
				.create()));
		
		putStaticComponent(new Button(28,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, gradient, length of 40",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(GradientUtils.applyGradient("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",GradientUtils.SAND_TO_BLUE_REVERSE),40)
				.create()));
		
		putStaticComponent(new Button(29,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, gradient, length of 20",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(GradientUtils.applyGradient("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, eu maximus arcu egestas in. Suspendisse potenti. Fusce non laoreet "
						+ "odio. Aliquam non lacus at diam suscipit condimentum eu et magna. Pellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas. Duis dui odio, pellentesque sit amet consequat "
						+ "sit amet, egestas et tortor. Mauris ut ultricies dui. Duis vulputate justo ut bibendum iaculis. Aenean "
						+ "et sem ultricies, dignissim nibh eu, consectetur nulla. Curabitur eu ultricies odio. Morbi nec augue id "
						+ "nunc imperdiet varius. Curabitur commodo nibh id gravida congue. Donec nec mi et neque malesuada suscipit. "
						+ "Maecenas euismod nunc imperdiet orci scelerisque aliquet.",GradientUtils.SAND_TO_BLUE_REVERSE),20)
				.create()));
		
		//////////////////////////////////////////////////////////////
		
		putStaticComponent(new Button(15,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, no colour, default length of 60, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",60)
				.create()));
		
		putStaticComponent(new Button(16,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, no colour, length of 40, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",40)
				.create()));
		
		putStaticComponent(new Button(17,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, no colour, length of 20, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",20)
				.create()));
		
		putStaticComponent(new Button(24,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, custom colour, default length of 60, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(ColourUtils.applyColour("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",ColourUtils.MELON),60)
				.create()));
		
		putStaticComponent(new Button(25,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, custom colour, length of 40, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(ColourUtils.applyColour("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",ColourUtils.MELON),40)
				.create()));
		
		putStaticComponent(new Button(26,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, custom colour, length of 20, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(ColourUtils.applyColour("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",ColourUtils.MELON),20)
				.create()));
		
		putStaticComponent(new Button(33,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, gradient, default length of 60, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(GradientUtils.applyGradient("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",GradientUtils.SAND_TO_BLUE_REVERSE),60)
				.create()));
		
		putStaticComponent(new Button(34,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, gradient, length of 40, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(GradientUtils.applyGradient("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",GradientUtils.SAND_TO_BLUE_REVERSE),40)
				.create()));
		
		putStaticComponent(new Button(35,ItemBuilder.of(Material.STRUCTURE_VOID)
				.name("Text, gradient, length of 20, & and & escapes",StdCols.TEXT_TITLE_INFO)
				.lore()
				.wrap(GradientUtils.applyGradient("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam eu est elementum nisi hendrerit ultricies. "
						+ "Mauris interdum scelerisque purus, \\&aeu maximus arcu egestas in. \\&bSuspendisse potenti. & Fusce non laoreet "
						+ "odio. \\&cAliquam non & && lacus at&&&& diam suscipit  \\&dcondimentum eu et magna. &\\&ePellentesque habitant morbi tristique "
						+ "senectus et netus et malesuada fames ac turpis egestas.\\&f&&&& Duis dui odio, pellentesque sit amet consequat.",GradientUtils.SAND_TO_BLUE_REVERSE),20)
				.create()));
	}

	@Override
	public void buildGUI(InventoryView view) {}
}
