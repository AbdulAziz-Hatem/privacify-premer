#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
#  sync-upstream.sh — مزامنة Fork privacify-premer مع upstream
#  الاستخدام:  bash scripts/sync-upstream.sh
#  يتطلب: gh (مصادق), git — كلاهما متوفر في Termux
# ============================================================
set -e

FORK="AbdulAziz-Hatem/privacify-premer"
UPSTREAM_URL="https://github.com/robinsrk/privacify.git"
WORK="$HOME/privacify-sync"

echo "=== 1) تجهيز نسخة عمل ==="
rm -rf "$WORK"
gh repo clone "$FORK" "$WORK" -- --depth=50
cd "$WORK"
git remote add upstream "$UPSTREAM_URL"

echo "=== 2) جلب أحدث upstream ==="
git fetch upstream main

CUR=$(git rev-parse HEAD)
NEW=$(git rev-parse upstream/main)
if [ "$CUR" = "$NEW" ]; then
    echo "✓ الـ Fork متزامن بالفعل مع upstream — لا يوجد جديد."
    exit 0
fi

echo ""
echo "=== 3) commits الجديدة في upstream (غير موجودة في Forkنا) ==="
git log --oneline "$CUR".."$NEW" | head -30

echo ""
echo "=== 4) دمج upstream في main ==="
if git merge upstream/main -m "Merge upstream: $(git log -1 --format='%h %s' upstream/main)"; then
    echo "✓ الدمج نجح بدون تعارضات"
else
    echo ""
    echo "⚠️  ⚠️  ⚠️  توجد تعارضات يجب حلها يدوياً!"
    echo "    الملفات المتعارضة:"
    git diff --name-only --diff-filter=U
    echo ""
    echo "    بعد الحل: git add <files> && git commit"
    echo "    ثم: git push origin main"
    exit 1
fi

echo ""
echo "=== 5) الملفات التي تغيرت (راجعها للترجمة) ==="
git diff --name-only "$CUR"..HEAD | grep -E '\.kt$|strings\.xml$' | grep -vE '^\.github/' || true

echo ""
echo "=== 6) الدفع إلى الـ Fork ==="
git push origin main
echo ""
echo "✅ تمت المزامنة. آخر commit: $(git log --oneline -1)"
echo "ملاحظة: إذا أضاف upstream نصوصاً إنجليزية جديدة،"
echo "يجب ترجمتها في app/src/main/res/values-ar/strings.xml"
