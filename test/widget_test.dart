import 'package:archive_flutter/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:liquid_glass_widgets/liquid_glass_widgets.dart';

void main() {
  testWidgets('Archive feed and liquid glass actions render on launch', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(
      LiquidGlassWidgets.wrap(
        brightnessResolver: Theme.maybeBrightnessOf,
        child: const ArchiveApp(),
      ),
    );
    await tester.pump();

    expect(find.text('Serein'), findsWidgets);
    expect(find.textContaining('周末的下午在街角咖啡馆'), findsOneWidget);
    expect(find.byType(GlassButton), findsOneWidget);
  });
}
