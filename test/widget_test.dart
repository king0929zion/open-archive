import 'package:archive_flutter/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('Archive feed and primary actions render on launch', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const ArchiveApp());
    await tester.pump();

    expect(find.text('Serein'), findsWidgets);
    expect(find.textContaining('周末的下午在街角咖啡馆'), findsOneWidget);
    expect(find.byType(InkWell), findsWidgets);
  });
}
