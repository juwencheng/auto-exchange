#!/bin/bash

# 版本同步脚本
# 用法: ./set_version.sh <新版本号>
# 例如: ./set_version.sh 1.1.0

set -e  # 遇到错误时退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查参数
if [ $# -eq 0 ]; then
    print_error "请提供新版本号"
    echo "用法: $0 <新版本号>"
    echo "例如: $0 1.1.0"
    exit 1
fi

NEW_VERSION=$1

# 验证版本号格式 (简单验证)
if [[ ! $NEW_VERSION =~ ^[0-9]+\.[0-9]+(\.[0-9]+)?(-[A-Za-z0-9.-]+)?(\+[A-Za-z0-9.-]+)?$ ]]; then
    print_error "版本号格式不正确: $NEW_VERSION"
    echo "支持的格式: x.y.z, x.y.z-SNAPSHOT, x.y.z-alpha.1 等"
    exit 1
fi

print_info "开始更新版本号到: $NEW_VERSION"

# 获取脚本所在目录的上级目录（项目根目录）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

print_info "项目根目录: $PROJECT_ROOT"

# 备份原始文件
backup_file() {
    local file=$1
    local backup_file="${file}.backup.$(date +%Y%m%d_%H%M%S)"
    cp "$file" "$backup_file"
    print_info "已备份: $file -> $backup_file"
}

# 更新pom文件中的版本号
update_pom_version() {
    local pom_file=$1
    local old_version=$2
    local new_version=$3
    
    print_info "更新: $pom_file"
    
    # 备份文件
    backup_file "$pom_file"
    
    # 使用sed更新版本号
    # 1. 更新 <version> 标签中的版本号
    # 2. 更新父模块引用中的版本号
    sed -i.tmp \
        -e "s/<version>$old_version<\/version>/<version>$new_version<\/version>/g" \
        -e "s/<version>$old_version<\/version>/<version>$new_version<\/version>/g" \
        "$pom_file"
    
    # 删除临时文件
    rm -f "${pom_file}.tmp"
    
    print_success "已更新: $pom_file"
}

# 获取当前版本号
get_current_version() {
    local pom_file=$1
    # 使用grep查找项目版本号，排除父模块版本
    # 查找 <artifactId>auto-exchange</artifactId> 后的版本号
    local version=$(awk '
    /<artifactId>auto-exchange<\/artifactId>/ {
        # 读取下一行，查找version标签
        getline
        if ($0 ~ /<version>[^<]*<\/version>/) {
            gsub(/<version>/, "", $0)
            gsub(/<\/version>/, "", $0)
            gsub(/^[[:space:]]+|[[:space:]]+$/, "", $0)
            print $0
            exit
        }
    }' "$pom_file")
    
    # 如果没找到，尝试查找其他artifactId后的版本号
    if [ -z "$version" ]; then
        version=$(awk '
        /<artifactId>/ && !/<artifactId>spring-boot-starter-parent<\/artifactId>/ {
            # 读取下一行，查找version标签
            getline
            if ($0 ~ /<version>[^<]*<\/version>/) {
                gsub(/<version>/, "", $0)
                gsub(/<\/version>/, "", $0)
                gsub(/^[[:space:]]+|[[:space:]]+$/, "", $0)
                print $0
                exit
            }
        }' "$pom_file")
    fi
    
    echo "$version"
}

# 主函数
main() {
    # 检查是否在项目根目录
    if [ ! -f "$PROJECT_ROOT/pom.xml" ]; then
        print_error "未找到根pom.xml文件，请确保在正确的项目目录中运行此脚本"
        exit 1
    fi
    
    # 获取当前版本号
    CURRENT_VERSION=$(get_current_version "$PROJECT_ROOT/pom.xml")
    print_info "当前版本: $CURRENT_VERSION"
    print_info "新版本: $NEW_VERSION"
    
    if [ "$CURRENT_VERSION" = "$NEW_VERSION" ]; then
        print_warning "版本号已经是 $NEW_VERSION，无需更新"
        exit 0
    fi
    
    # 更新根pom.xml
    print_info "更新根pom.xml..."
    update_pom_version "$PROJECT_ROOT/pom.xml" "$CURRENT_VERSION" "$NEW_VERSION"
    
    # 查找所有子模块的pom.xml文件
    print_info "查找子模块pom文件..."
    SUBMODULE_POMS=(
        "$PROJECT_ROOT/auto-exchange-spring-boot-autoconfigure/pom.xml"
        "$PROJECT_ROOT/auto-exchange-spring-boot-core/pom.xml"
        "$PROJECT_ROOT/auto-exchange-spring-boot-starter/pom.xml"
        "$PROJECT_ROOT/auto-exchange-spring-boot-test-app/pom.xml"
    )
    
    # 更新子模块pom文件
    for pom_file in "${SUBMODULE_POMS[@]}"; do
        if [ -f "$pom_file" ]; then
            # 获取子模块的当前版本
            sub_version=$(get_current_version "$pom_file")
            if [ "$sub_version" = "$CURRENT_VERSION" ]; then
                update_pom_version "$pom_file" "$CURRENT_VERSION" "$NEW_VERSION"
            else
                print_warning "跳过 $pom_file (版本不匹配: $sub_version != $CURRENT_VERSION)"
            fi
        else
            print_warning "文件不存在: $pom_file"
        fi
    done
    
    print_success "版本更新完成！"
    print_info "所有pom文件已更新到版本: $NEW_VERSION"
    print_info "备份文件已创建，如需回滚请查看 .backup.* 文件"
    
    # 显示更新摘要
    echo
    print_info "更新摘要:"
    echo "  根pom.xml: $CURRENT_VERSION -> $NEW_VERSION"
    for pom_file in "${SUBMODULE_POMS[@]}"; do
        if [ -f "$pom_file" ]; then
            echo "  $(basename "$(dirname "$pom_file")")/pom.xml: $CURRENT_VERSION -> $NEW_VERSION"
        fi
    done
}

# 运行主函数
main "$@"
